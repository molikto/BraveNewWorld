package org.snailya.bnw.gamelogic

import org.snailya.base.*
import org.snailya.bnw.gamelogic.mapgen.MapGen
import java.util.*


private val RelativeCorners = arrayOf(ivec2(-1, -1), ivec2(1, -1), ivec2(-1, 1), ivec2(1, 1))
private val CornerCost = StrictMath.sqrt(2.0).toFloat()
private val RelativeSides =  arrayOf(ivec2(-1, 0), ivec2(1, 0), ivec2(0, 1), ivec2(0, -1))


/**
 *
 * the map will have unwalk-able areas around it now, so no need to check pointer over-bound
 */
class Map() {

    val random = game.random

    // tiles 0.... 99, metrics 0..1...100
    val size = 200

    // states
    val debug_mapGen = MapGen(random, size)
    val map: Array<Array<Tile>> = debug_mapGen.gen()

    fun randomTile() = map[random.nextInt(size)][random.nextInt(size)]

    inline operator fun invoke(i: IntVector2) = map[i.x][i.y]
    inline operator fun invoke(i: StrictVector2) = map[i.x.toInt()][i.y.toInt()]

    inline operator fun invoke(x: Int, y: Int) = map[x][y]
    inline operator fun invoke(x: Float, y: Float) = map[x.toInt()][y.toInt()]

    fun inBound(x: Int, y: Int) = x >= 0 && x < size && y >= 0 && y < size
    fun inBound(a: IntVector2) = inBound(a.x, a.y)
    fun inBound(s: StrictVector2) = inBound(s.x.toInt(), s.y.toInt())


    // TODO return the hit point
    @Strictfp
    fun hitWall(a: StrictVector2, b: StrictVector2): Tile? {
        if (a.x < 0 || a.y < 0 || a.x >= size || a.y >= size) return null
        if (this(a).walled) return this(a)
        val higher = if (a.y > b.y) a else b
        val lower = if (a.y > b.y) b else a
        val vertical = a.x == b.x
        val slope = if (vertical) 0F else (a.y - b.y) / (a.x - b.x)
        for (i in Math.max(0, StrictMath.ceil(lower.y.toDouble()).toInt()) .. Math.min(size - 1, higher.y.toInt())) {
            val y = i
            val fx = if (vertical) a.x else ((y - a.y) / slope + a.x)
            val x = fx.toInt()
            if (x.toFloat() == fx) { // a exact value
                if (slope > 0F) {
                    var can1 = this(x, y)
                    if (can1.walled) return can1
                    can1 = this(x - 1, y - 1)
                    if (can1.walled) return can1
                    can1 = this(x, y - 1)
                    val can2 = this(x - 1, y)
                    if (can1.walled && can2.walled) return if (random.nextBoolean()) can1 else can2
                } else if (slope < 0F) {
                    var can1 = this(x - 1, y)
                    if (can1.walled) return can1
                    can1 = this(x, y - 1)
                    if (can1.walled) return can1
                    can1 = this(x, y)
                    val can2 = this(x - 1, y - 1)
                    if (can1.walled && can2.walled) return if (random.nextBoolean()) can1 else can2
                } else {
                    // TODO... make it better, whatever
                    var can1 = this(x, y)
                    if (can1.walled) return can1
                    can1 = this(x - 1, y - 1)
                    if (can1.walled) return can1
                    can1 = this(x - 1, y)
                    if (can1.walled) return can1
                    can1 = this(x, y - 1)
                    if (can1.walled) return can1
                }
            } else {
                var can1 = this(x, y)
                if (can1.walled) return can1
                can1 = this(x, y - 1)
                if (can1.walled) return can1
            }
        }
        return null
    }




    // all variables is temp
    inner class FindRouteWrapper {

        private val map = this@Map

        private var counter = -1
        val debug_counter get() = counter

        private val pq = PriorityQueue<Tile>(30, object : Comparator<Tile> {

            @Strictfp
            override fun compare(o1: Tile, o2: Tile): Int {
                if (o1.temp_priority > o2.temp_priority) {
                    return 1
                } else if (o1.temp_priority < o2.temp_priority) {
                    return -1
                }
                return 0
            }
        })
        private var tpos = svec2()
        private var ipos = ivec2()


        @Strictfp operator fun invoke(position: StrictVector2, dest: IntVector2, /* out */ route: MutableList<Tile>) {
            val dt = map(dest)
            if (dt.notWalkable) {
                println("glitch: dest is notWalkable")
                return
            }
            route.clear()
            this.counter++
            pq.clear()
            ipos.set(position)
            run {
                val next = map(ipos)
                next.center(tpos)
                val cost = position.dis(tpos)
                util_tryAddRoute(next, dt, IntVector2.Zero, cost, false)
            }
            for (vec in RelativeCorners) {
                val next = map(ipos.x + vec.x, ipos.y + vec.y)
                val next0 = map(ipos.x + vec.x, ipos.y)
                val next1 = map(ipos.x, ipos.y + vec.y)
                if (!next.notWalkable && !next0.notWalkable && !next1.notWalkable) {
                    next.center(tpos)
                    val cost = position.dis(tpos)
                    util_tryAddRoute(next, dt, IntVector2.Zero, cost, false)
                }
            }
            for (vec in RelativeSides) {
                val next = map(ipos.x + vec.x, ipos.y + vec.y)
                if (!next.notWalkable) {
                    next.center(tpos)
                    val cost = position.dis(tpos)
                    util_tryAddRoute(next, dt, IntVector2.Zero, cost, false)
                }
            }
            while (!pq.isEmpty()) {
                val nearest = pq.poll()!!
                if (nearest.position == dt.position) {
                    var t = nearest
                    while (true) {
                        route.add(t)
                        if (t.temp_ttpo != IntVector2.Zero) {
                            t = map(t.position.x - t.temp_ttpo.x, t.position.y - t.temp_ttpo.y)
                        } else {
                            break
                        }
                    }
                    return
                }
                ipos.set(nearest.position)
                for (vec in RelativeCorners) {
                    val next = map(ipos.x + vec.x, ipos.y + vec.y)
                    val next0 = map(ipos.x + vec.x, ipos.y)
                    val next1 = map(ipos.x, ipos.y + vec.y)
                    if (!next.notWalkable && !next0.notWalkable && !next1.notWalkable) {
                        util_tryAddRoute(next, dt, vec, nearest.temp_cost + CornerCost, true)
                    }
                }
                for (vec in RelativeSides) {
                    val next = map(ipos.x + vec.x, ipos.y + vec.y)
                    if (!next.notWalkable) {
                        util_tryAddRoute(next, dt, vec, nearest.temp_cost + 1F, true)
                    }
                }
            }
            return // no route
        }

        private val hpos = ivec2()

        private fun util_tryAddRoute(next: Tile, dt: Tile, vec: IntVector2, cost: Float, h: Boolean) {
            val new = next.temp_visited != counter
            if (new || cost < next.temp_cost) {
                if (!new) pq.remove(next)
                next.temp_cost = cost
                val p = if (h) {
                    hpos.set(next.position)
                    hpos - dt.position
                    val x = Math.abs(hpos.x)
                    val y = Math.abs(hpos.y)
                    val max = Math.max(x, y)
                    val min = Math.min(x, y)
                    val remainingDis = (max - min) + min * CornerCost
                    remainingDis
                } else {
                    0F
                }
                next.temp_priority = cost + p
                next.temp_visited = counter
                next.temp_ttpo = vec
                pq.add(next)
            }
        }

    }
    val findRoute = FindRouteWrapper()
}
