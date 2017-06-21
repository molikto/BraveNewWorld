package org.snailya.bnw.gamelogic

import org.serenaz.Edge
import org.serenaz.Edge.clamp
import org.serenaz.InputPoint
import org.serenaz.Point
import org.serenaz.Voronoi
import org.snailya.base.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


/**
 * this class use a Voronoi generation algorithm found from GitHub
 * this algorithm is NOT intended to be used in other places
 */
class MapGen(val random: Random, val size: Int) {

    lateinit var debug_edges: List<Edge>
    lateinit var debug_points: List<InputPoint>


    fun nonZeroDouble(): Double {
        val a = 0.001
        val b = (1 - a * 2)
        return a + b * random.nextDouble()
    }

    @Strictfp
    fun gen(): Array<Array<MapTile>> {
        /**
         * generate the Voronoi polygons
         */
        var randomDots = ArrayList<InputPoint>()
        val nPoints = size * size / 10
        for (i in 0 .. nPoints) {
            randomDots.add(InputPoint(nonZeroDouble(), nonZeroDouble()))
        }
        var voronoi: Voronoi? = null
        for (i in 0 until 1) {
            voronoi = time("generating Voronoi diagram") { Voronoi(randomDots) }
            for (p in voronoi.sites) {
                p.x = 0.0
                p.y = 0.0
            }
            for (e in voronoi.edges) {
                // using the x, y value as a hack... to save memory
                // this is a very specific hack and not general
                // assert(e.clamp())
                val x = clamp(e.start.x) + clamp(e.end.x)
                val y = clamp(e.start.y) + clamp(e.end.y)
                e.site_left.x += x
                e.site_right.x += x
                e.site_left.y += y
                e.site_right.y += y
                e.site_left.edgeCount += 1
                e.site_right.edgeCount += 1
            }
            randomDots = ArrayList()
            voronoi.sites.mapTo(randomDots) { InputPoint(it.x / it.edgeCount / 2, it.y / it.edgeCount / 2) }
        }
        val v = voronoi!!
        for (p in v.sites) {
            // again, another hack
            p.attachment = InputPoint.Attachment()
        }
        debug_edges = v.edges
        debug_points = v.sites

        /**
         * set all edge points to sea
         */
        for (e in v.edges) {
            e.site_left.attachment.edges.add(e)
            e.site_right.attachment.edges.add(e)
            if (e.isSea()) {
                e.site_left.attachment.isSea = true
                e.site_right.attachment.isSea = true
            }
        }

        /**
         * make the sea not that square
         *
         * randomly set some beach points to sea
         * // TODO more interesting
         */
        v.sites.filter { !it.attachment.isSea && it.attachment.nearSea() }
                .forEach { if (random.nextInt(3) == 1) it.attachment.isSea = true }

        /**
         * beach line
         */
        val beaches = v.sites.filter { !it.attachment.isSea && it.attachment.nearSea() }
                .map { it.attachment.isBeach = true; it.attachment.height = 0; it }

        /**
         * height
         */
        val queue = LinkedList<InputPoint>()
        queue.addAll(beaches)
        while (!queue.isEmpty()) {
            val it = queue.poll()
            if (!it.attachment.isSea) {
                for (e in it.attachment.edges) {
                    val other = e.otherSide(it)
                    if (other != null && other.attachment.height == -1) {
                        // 2 flat
                        // 2 go up
                        // 1 go down
                        val i = random.nextInt(5)
                        val add = if (i < 2) {
                            0
                        } else if (i < 4) {
                            1
                        } else {
                            -1
                        }
                        other.attachment.height = maxOf(0, minOf(it.attachment.height, 5) + add)
                        queue.add(other)
                    }
                }
            }
        }

        return rasterize(v.sites)
    }



    val xFirstComparator = Comparator<InputPoint> { o1, o2 ->
        if (o1.x > o2.x) 1 else if (o1.x < o2.x) -1 else {
            if (o1.y > o2.y) 1 else if (o1.y < o2.y) -1 else 0
        }
    }
    val yFirstComparator = Comparator<InputPoint> { o1, o2->
        if (o1.y > o2.y) 1 else if (o1.y < o2.y) -1 else {
            if (o1.x > o2.x) 1 else if (o1.x < o2.x) -1 else 0
        }
    }

    var currentBest: Double = 0.0
    var currentPoint: InputPoint? = null

    @Strictfp
    fun kdTree(sites: MutableList<InputPoint>, useX: Boolean): InputPoint? {
        if (sites.size == 0) {
            return null
        } else if (sites.size == 1) {
            return sites[0]
        } else {
            sites.sortWith(if (useX) xFirstComparator else yFirstComparator)
            val mi = sites.size / 2
            val m = sites[mi]
            m.attachment.left = kdTree(sites.subList(0, mi), !useX)
            m.attachment.right = kdTree(sites.subList(mi + 1, sites.size), !useX)
            return m
        }
    }

    @Strictfp
    private fun rasterize(sites: MutableList<InputPoint>): Array<Array<MapTile>> {
        val map: Array<Array<MapTile>> = Array(size, { x -> Array(size, { y -> configured(MapTile()) { position = ivec2(x, y) } }) })
        for (s in sites) {
            s.x *= size
            s.y *= size
        }
        val root = kdTree(sites, true)!!
        val testPoint = InputPoint(0.0, 0.0)
        for (i in 0 until size) {
            for (j in 0 until size) {
                testPoint.x = i + 0.5
                testPoint.y = j + 0.5
                currentBest = Double.MAX_VALUE
                search(root, testPoint, true)
                map[i][j].groundType = groundTypeOf(currentPoint!!)
            }
        }
        return map
    }


    private fun  search(root: InputPoint, x: InputPoint, useX: Boolean) {
        val res = (if (useX) xFirstComparator else yFirstComparator).compare(root, x)
        if (res > 0) {
            val can = root.attachment.right
            val o = root.attachment.left
            if (can != null) {
                doRest(root, can, o, x, useX)
            } else {
                searchedLeaf(root, x)
            }
        } else if (res < 0) {
            val can = root.attachment.left
            val o = root.attachment.right
            if (can != null) {
                doRest(root, can, o, x, useX)
            } else {
                searchedLeaf(root, x)
            }
        } else {
            currentBest = 0.0
            currentPoint = root
        }
    }

    private fun  doRest(root: InputPoint, can: InputPoint, o: InputPoint?, x: InputPoint, b: Boolean) {
        search(can, x, !b)
        val dis2 = dis2(root, x)
        if (dis2 < currentBest) {
            currentBest = dis2
            currentPoint = root
        }
        val c = if (b) can.x - root.x else can.y - root.y
        if (dis2 > c * c && o != null) {
            search(o, x, !b)
        }

    }

    private fun searchedLeaf(root: InputPoint, x: InputPoint) {
        currentBest = dis2(root, x)
        currentPoint = root
    }

    private fun  dis2(root: InputPoint, x: InputPoint): Double {
        val a = root.x - x.x
        val b = root.y - x.y
        return a * a + b * b
    }


    /*
    abstract class Event() {
    }

    class EdgeStartEvent(@JvmField val e: Edge) : Event()

    class EdgeEndEvent(@JvmField val e: Edge) : Event()

    class SampleEvent() : Event()

    // THIS IS NOT WORKING YET....
    @Strictfp
    private fun rasterize2(edges: List<Edge>): Array<Array<MapTile>> {
        val map: Array<Array<MapTile>> = Array(size, { x ->  Array(size, { y -> configured(MapTile()) { position = ivec2(x, y) } })})

        fun <T> add(a: TreeMap<Double, ArrayList<T>>, d: Double,  t: T) {
            var l = a[d]
            if (l == null) {
                l = ArrayList<T>()
                a.put(d, l)
            }
            l.add(t)
        }
        val events = TreeMap<Double, ArrayList<Event>>()
        for (e in edges) {
            e.yint *= size
            e.start.x * size
            e.end.x * size
            e.start.y * size
            e.end.y * size
            val start: Double
            val end: Double
            if (e.start.x > e.end.x) {
                start = e.end.x
                end = e.start.x
            } else if (e.start.x < e.end.x) {
                start = e.start.x
                end = e.end.x
            } else {
                start = 0.0
                end = 0.0
            }
            if (start != end) {
                add(events, start, EdgeStartEvent(e))
                add(events, end, EdgeEndEvent(e))
            }
        }
        for (i in 0 until size) {
            add(events, i + 0.5, SampleEvent())
        }
        val edges = TreeSet<Edge>(Comparator<Edge> { o1, o2 ->
            if (o1.samplePointY > o2.samplePointY)  {
                1
            } else if (o1.samplePointY < o2.samplePointY) {
                -1
            } else {
                if (o1.slope > o2.slope) {
                    1
                } else if (o1.slope < o2.slope) {
                    -1
                } else {
                    if (o1.start.x > o2.start.x) {
                        1
                    } else if (o1.start.x < o2.start.x) {
                        -1
                    } else {
                        assert(o1 === o2)
                        0
                    }
                }
            }
        })
        var pendingX = 0.5
        var bottom: InputPoint? = null
        while (!events.isEmpty()) {
            val entry = events.pollFirstEntry()
            for (ev in entry.value) {
                when (ev) {
                    is EdgeStartEvent -> {
                        println("start ${ev.e}")
                        ev.e.samplePointY = ev.e.slope * pendingX + ev.e.yint
                        edges.add(ev.e)
                    }
                    is EdgeEndEvent -> {
                        println("end ${ev.e}")
                        edges.remove(ev.e)
                        //assert(edges.remove(ev.e))
                    }
                    is SampleEvent -> {
                        println("sample $pendingX")
                        val toAdd = map[pendingX.toInt()]
                        var y = 0.5
                        for (e in edges) {
                            if (e.samplePointY in 0..size) {
                                val end = maxOf(e.start.x, e.end.x)
                                if (end < pendingX) continue
                                //if (end < pendingX) throw IllegalStateException("what ${pendingX} $e")
                                val top = if (e.site_left.y > e.site_right.y) e.site_right else e.site_left
                                bottom = if (e.site_left.y > e.site_right.y) e.site_left else e.site_right
                                val groundType = groundTypeOf(top)
                                while (y < size && y <= e.samplePointY) {
                                    val tile = toAdd[y.toInt()]
                                    tile.groundType = groundType
                                    y += 1
                                }
                            }
                        }
                        // we will just assume we will have a edge pointed to the left side,
                        // this is statistically very possible
                        assert(bottom != null)
                        val groundType = groundTypeOf(bottom!!)
                        while (y < size) {
                            val tile = toAdd[y.toInt()]
                            tile.groundType = groundType
                            y += 1
                        }
                        bottom = null
                        pendingX += 1
                        for (e in edges) e.samplePointY =  e.slope * pendingX + e.yint
                    }
                }
            }
        }
        return map
    }

    */

    fun groundTypeOf(top: InputPoint) : GroundType =
        if (top.attachment.isSea) GroundType.Ocean
        else if (top.attachment.isBeach) GroundType.Sand
        else if (top.attachment.height < 2) GroundType.Grassland
        else if (top.attachment.height < 3) GroundType.Highland
        else GroundType.Mountain

}

@Strictfp inline fun outside(p: Point) = p.x <= 0 || p.y <= 0 || p.x >= 1 || p.y >= 1

fun Edge.isSea() =  outside(this.start) || outside(this.end)
