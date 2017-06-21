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

        return rasterize(v.edges)
    }


    abstract class Event(@JvmField val p: Double) : Comparable<Event> {
        override fun compareTo(other: Event): Int {
            if (p > other.p) return 1
            else if (p < other.p) return -1
            else return 0
        }
    }

    class EdgeStartEvent(@JvmField val e: Edge,  p: Double) : Event(p)

    class EdgeEndEvent(@JvmField val e: Edge, p: Double) : Event(p)

    class SampleEvent(p: Double) : Event(p)

    @Strictfp
    private fun rasterize(edges: List<Edge>): Array<Array<MapTile>> {
        val events = TreeSet<Event>()
        for (e in edges) {
            e.yint *= size
            if (e.start.x > e.end.x) {
                events.add(EdgeStartEvent(e, e.end.x * size))
                events.add(EdgeEndEvent(e, e.start.x * size))
            } else {
                events.add(EdgeEndEvent(e, e.end.x * size))
                events.add(EdgeStartEvent(e, e.start.x * size))
            }
        }
        val map: Array<Array<MapTile>> = Array(size, { x ->  Array(size, { y -> configured(MapTile()) { position = ivec2(x, y) } })})
        for (i in 0 until size) {
            events.add(SampleEvent(i + 0.5))
        }
        val edges = TreeSet<Edge>(Comparator<Edge> { o1, o2 ->
            if (o1.samplePointY > o2.samplePointY)  {
                1
            } else if (o1.samplePointY < o2.samplePointY) {
                -1
            } else {
                0
            }
        })
        var pendingX = 0.5
        while (!events.isEmpty()) {
            val ev = events.pollFirst()
            when (ev) {
                is EdgeStartEvent -> {
                    ev.e.samplePointY = ev.e.slope * pendingX + ev.e.yint
                    edges.add(ev.e)
                }
                is EdgeEndEvent -> edges.remove(ev.e)
                is SampleEvent -> {
                    val toAdd = map[ev.p.toInt()]
                    var y = 0.5
                    var bottom: InputPoint? = null
                    for (e in edges) {
                        val end = maxOf(e.start.x, e.end.x) * size
                        if (end < ev.p) continue
                        val top = if (e.site_left.y > e.site_right.y) e.site_right else e.site_left
                        bottom = if (e.site_left.y > e.site_right.y) e.site_left else e.site_right
                        val groundType = groundTypeOf(top)
                        while (y < size && y <= e.samplePointY) {
                            val tile = toAdd[y.toInt()]
                            tile.groundType = groundType
                            y += 1
                        }
                    }
                    val groundType = groundTypeOf(bottom!!)
                    while (y < size) {
                        val tile = toAdd[y.toInt()]
                        tile.groundType = groundType
                        y += 1
                    }
                    pendingX = ev.p + 1
                    for (e in edges) e.samplePointY =  e.slope * pendingX + e.yint
                }
            }
        }
        return map
    }

    fun groundTypeOf(top: InputPoint) : GroundType =
        if (top.attachment.isSea) GroundType.Ocean
        else if (top.attachment.isBeach) GroundType.Sand
        else if (top.attachment.height < 2) GroundType.Grassland
        else if (top.attachment.height < 3) GroundType.Highland
        else GroundType.Mountain

}

@Strictfp inline fun outside(p: Point) = p.x <= 0 || p.y <= 0 || p.x >= 1 || p.y >= 1

fun Edge.isSea() =  outside(this.start) || outside(this.end)
