package org.snailya.bnw.gamelogic

import org.serenaz.InputPoint
import org.snailya.base.*
import org.snailya.bnw.*
import java.util.*
import kotlin.Comparator


open class AgentConfig : WalkerConfig() {
}

open class WalkerConfig {
    var speed: Float = 1F.ps
}


/**
 * https://en.wikipedia.org/wiki/Soil
 */
//enum class GroundType {
//    Snow, Sand, Soil, SoilRich,
////    Snow, Tundra, Bare, Scorched,
////    Taiga, Shrubland, TemperateDesert,
////    TemperateRainForest, TemperateDec, Grassland,
////    TropicalRainForest, TropicalSeasonalForest, SubtropicalDesert
//
//}

enum class GroundType {
    Ocean, Sand, Grassland, Highland, Mountain
}


class MapTile {
    lateinit var position: IntVector2
    @Strictfp
    inline fun center(s: StrictVector2): StrictVector2 {
        s.x = position.x + 0.5F
        s.y = position.y + 0.5F
        return s
    }
    val notWalk: Boolean
        get() = groundType == GroundType.Mountain || groundType == GroundType.Highland || groundType == GroundType.Ocean
    lateinit var groundType: GroundType
    lateinit var debug_inputPoint: InputPoint

    var temp_cost: Float = 0F
    var temp_priority: Float = 0F
    var temp_visited: Int = -1
    var temp_ttpo: IntVector2 = IntVector2.Zero // to the previous of
}

class BnwGame(val myIndex: Int, val playerSize: Int, seed: Long) {



    /**
     *
     * basic
     */
    val random = Random(seed)

    /**
     * map
     *
     * the map will have unwalk-able areas around it now, so no need to check pointer over-bound
     */

    val relativeCorners = arrayOf(ivec2(-1, -1), ivec2(1, -1), ivec2(-1, 1), ivec2(1, 1))
    val cornerCost = StrictMath.sqrt(2.0).toFloat()
    val relativeSides =  arrayOf(ivec2(-1, 0), ivec2(1, 0), ivec2(0, 1), ivec2(0, -1))





    inner class Map {
        // tiles 0.... 99, metrics 0..1...100
        val size = 100
        val debug_mapGen = MapGen(random, size)
        val map: Array<Array<MapTile>> = debug_mapGen.gen()



        fun random() = map[random.nextInt(size)][random.nextInt(size)]

        inline operator fun invoke(i: IntVector2) = map[i.x][i.y]
        inline operator fun invoke(i: StrictVector2) = map[i.x.toInt()][i.y.toInt()]

        inline operator fun invoke(x: Int, y: Int) = map[x][y]
        inline operator fun invoke(x: Float, y: Float) = map[x.toInt()][y.toInt()]


        // TODO return the hit point
        @Strictfp
        fun hitWall(a: StrictVector2, b: StrictVector2): MapTile? {
            if (a.x < 0 || a.y < 0 || a.x >= size || a.y >= size) return null
            if (this(a).notWalk) return this(a)
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
                        if (can1.notWalk) return can1
                        can1 = this(x - 1, y - 1)
                        if (can1.notWalk) return can1
                        can1 = this(x, y - 1)
                        val can2 = this(x - 1, y)
                        if (can1.notWalk && can2.notWalk) return if (random.nextBoolean()) can1 else can2
                    } else if (slope < 0F) {
                        var can1 = this(x - 1, y)
                        if (can1.notWalk) return can1
                        can1 = this(x, y - 1)
                        if (can1.notWalk) return can1
                        can1 = this(x, y)
                        val can2 = this(x - 1, y - 1)
                        if (can1.notWalk && can2.notWalk) return if (random.nextBoolean()) can1 else can2
                    } else {
                        // TODO... make it better, whatever
                        var can1 = this(x, y)
                        if (can1.notWalk) return can1
                        can1 = this(x - 1, y - 1)
                        if (can1.notWalk) return can1
                        can1 = this(x - 1, y)
                        if (can1.notWalk) return can1
                        can1 = this(x, y - 1)
                        if (can1.notWalk) return can1
                    }
                } else {
                    var can1 = this(x, y)
                    if (can1.notWalk) return can1
                    can1 = this(x, y - 1)
                    if (can1.notWalk) return can1
                }
            }
            return null
        }
    }; val map = Map()


    /**
     * bullets!!
     */

    val bullets = mutableListOf<Bullet>()

    inner class Bullet(val shooterFaction: Int, val initial: StrictVector2, to: StrictVector2) {
        val temp_pos: StrictVector2 = svec2()
        val maxLifetime = 1F
        var lifetime = maxLifetime
        val speed = 10F.ps
        val position: StrictVector2 = initial.copy()
        val tick = (to.copy() - initial).nor() * speed

        @Strictfp
        fun fly() {
            assert(lifetime > 0)
            temp_pos.set(position)
            position + tick
            // TODO hit wall
            if (map.hitWall(temp_pos, position) == null) {
                for (a in agents) {
                    if (a.faction == shooterFaction && initial.dis(position) <1.3F) {
                        // ignore this
                    } else {
                        val inter = a.intersects(temp_pos, position)
                        if (inter != null) {
                            a.injured((1 - inter) * 2F)
                            lifetime = 0F
                        }
                    }
                }
            } else {
                lifetime = 0F
            }
            lifetime -= 1F.ps
        }
    }

    /**
     * agents
     */
    val agentConfig = configured(AgentConfig()) {  }
    val agents = (0 until playerSize).map { index ->
        configured(Agent()) { faction = index; config =  agentConfig; position = svec2(0.5F, 0.5F) }
    }


    init {
        for (a in agents) {
            while (true) {
                val t = map.random()
                if (!t.notWalk) {
                    t.center(a.position)
                    break
                }
            }
        }
    }

    /**
     * tick! tick!
     */
    fun tick(commands: List<List<PlayerCommand>>?) {
        if (commands != null) {
            assert(commands.size == playerSize)
            for (i in 0 until playerSize) {
                val agent = agents[i]
                val cs = commands[i]
                for ((dest) in cs) {
                    agent.findRoute(dest)
                }
            }
        }
        for (b in bullets) {
            b.fly()
        }
        bullets.removeAll { it.lifetime <= 0 }
        for (a in agents) {
            a.tryFireOrLockOn()
            a.tryWalk()
        }
    }



    /**
     *
     *
     *
     * inner classes
     *
     *
     */

    // all variables is temp
    inner class FindRouteWrapper {
        var counter = -1
        val pq = PriorityQueue<MapTile>(30, object : Comparator<MapTile> {

            @Strictfp
            override fun compare(o1: MapTile, o2: MapTile): Int {
                if (o1.temp_priority > o2.temp_priority) {
                    return 1
                } else if (o1.temp_priority < o2.temp_priority) {
                    return -1
                }
                return 0
            }
        })
        var tpos = svec2()
        var ipos = ivec2()


        @Strictfp operator fun invoke(position: StrictVector2, dest: IntVector2, /* out */ route: MutableList<MapTile>) {
            val dt = map(dest)
            if (dt.notWalk) {
                println("glitch: dest is notWalk")
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
            for (vec in relativeCorners) {
                val next = map(ipos.x + vec.x, ipos.y + vec.y)
                val next0 = map(ipos.x + vec.x, ipos.y)
                val next1 = map(ipos.x, ipos.y + vec.y)
                if (!next.notWalk && !next0.notWalk && !next1.notWalk) {
                    next.center(tpos)
                    val cost = position.dis(tpos)
                    util_tryAddRoute(next, dt, IntVector2.Zero, cost, false)
                }
            }
            for (vec in relativeSides) {
                val next = map(ipos.x + vec.x, ipos.y + vec.y)
                if (!next.notWalk) {
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
                for (vec in relativeCorners) {
                    val next = map(ipos.x + vec.x, ipos.y + vec.y)
                    val next0 = map(ipos.x + vec.x, ipos.y)
                    val next1 = map(ipos.x, ipos.y + vec.y)
                    if (!next.notWalk && !next0.notWalk && !next1.notWalk) {
                        util_tryAddRoute(next, dt, vec, nearest.temp_cost + cornerCost, true)
                    }
                }
                for (vec in relativeSides) {
                    val next = map(ipos.x + vec.x, ipos.y + vec.y)
                    if (!next.notWalk) {
                        util_tryAddRoute(next, dt, vec, nearest.temp_cost + 1F, true)
                    }
                }
            }
            return // no route
        }

        val hpos = ivec2()

        inline private fun util_tryAddRoute(next: MapTile, dt: MapTile, vec: IntVector2, cost: Float, h: Boolean) {
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
                    val remainingDis = (max - min) + min * cornerCost
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

    }; val findRoute = FindRouteWrapper()

    class WalkWrapper {

        // temp
        val pos = svec2()
        @Strictfp operator fun invoke(walker: Walker, /* in-out */ route: MutableList<MapTile>, /* in-out */ position: StrictVector2) {
            // TODO re-plan when game structure changes
            if (!route.isEmpty()) {
                val id = route.last()
                id.center(pos)
                pos - position
                val dis = pos.len()
                val time = dis / walker.config.speed
                if (time < 1) { // we can finish this dis
                    // move the player to id
                    id.center(position)
                    // remove last
                    route.removeAt(route.size - 1)
                    if (!route.isEmpty()) {
                        // I don't think... we don't never have a situation where a creature can move multiple tiles per tick
                        val nid = route.last()
                        nid.center(pos)
                        pos - position
                        position + pos.nor() * walker.config.speed * (1 - time)
                    }
                } else {
                    position + pos.nor() * walker.config.speed
                }
            }
        }

    }; val walk = WalkWrapper()




    /**
     *
     *
     *
     *
     */


    inner class Agent() : Walker() {
        var faction: Int = -1
        // temp variables
        // constants
        val totalLockOnTime = 0.5F
        val maxLockOnDistance = 5F
        val maxHealth = 10F
        var health: Float = maxHealth
        var lockingOnTarget: Agent? = null
        var lockingOnTime = 0F

        @Strictfp
        fun tryFireOrLockOn() {
            if (walking) {
                lockingOnTarget = null
            } else {
                val target = lockingOnTarget
                if (target != null) {
                    val distance = target.position.dis(position)
                    if (distance <= maxLockOnDistance && map.hitWall(position, target.position) == null) {
                        lockingOnTime += 0.2F.ps
                        if (lockingOnTime >= totalLockOnTime) {
                            bullets.add(Bullet(faction, position, target.position))
                            lockingOnTarget = null
                            lockingOnTarget = null
                        }
                    } else {
                        lockingOnTarget = null
                    }
                } else {
                    // TODO find target more smart...
                    // TODO size of the target???
                    for (a in agents) {
                        if (a != this) {
                            val distance = a.position.dis(position)
                            if (distance <= maxLockOnDistance) {
                                if (map.hitWall(position, a.position) == null) {
                                    lockingOnTarget = a
                                    lockingOnTime = 0F
                                }
                            }
                        }
                    }
                }
            }
        }

        fun  injured(fl: Float) {
            health -= fl
        }
    }

    inner open class Walker {
        lateinit var config: WalkerConfig

        lateinit var position: StrictVector2
        val size = 0.5F // TODO now all stuff is actually round!
        val route = mutableListOf<MapTile>()
        inline val walking: Boolean
            get() = !route.isEmpty()

        fun findRoute(dest: IntVector2) {
          time("finding route") { findRoute(position, dest, route) }
        }

        val temp_pos = svec2()

        fun tryWalk() = walk(this, route, position)

        @Strictfp
        fun intersects(from: StrictVector2, to: StrictVector2): Float? {
            val dis = pointToLineDistance(from, to, position)
            if (dis < size) {
                val d2 = pointToLineSegmentDistance(from, to, position)
                if (d2 < size) {
                    // the reason we return dis is because this is where the line will hit most deep
                    return dis / size
                } else {
                    return null
                }
            } else {
                return null
            }
        }
    }


    /**
     *
     *
     * debug
     *
     *
     */
    fun  debug_hash(): Int {
        return agents.map { it.position.hashCode() }.sum()
    }
}





