package org.snailya.bnw.gamelogic

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

class MapTile {
    lateinit var position: IntVector2
    @Strictfp
    fun center(s: StrictVector2): StrictVector2 {
        s.x = position.x + 0.5F
        s.y = position.y + 0.5F
        return s
    }
    var rock = false

    var temp_cost: Float = 0F
    var temp_priority: Float = 0F
    var temp_visited: Int = -1
    var temp_ttpo: IntVector2 = IntVector2.Zero // to the previous of
}

val relativeCorners = arrayOf(ivec2(-1, -1), ivec2(1, -1), ivec2(-1, 1), ivec2(1, 1))
val cornerCost = StrictMath.sqrt(2.0).toFloat()
val relativeSides =  arrayOf(ivec2(-1, 0), ivec2(1, 0), ivec2(0, 1), ivec2(0, -1))


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
    inner class Map {
        // tiles 0.... 99, metrics 0..1...100
        val size = 100
        val map: Array<Array<MapTile>> = Array(size, { x ->  Array(size, { y -> configured(MapTile()) { rock = x == 0 || y == 0 || x == size - 1 || y == size - 1; position = ivec2(x, y) } })})


        fun random() = map[random.nextInt(size)][random.nextInt(size)]

        inline operator fun invoke(i: IntVector2) = map[i.x][i.y]

        inline operator fun invoke(x: Int, y: Int) = map[x][y]

        init { // temp generate map and players
            val hasBlocks = (0 until size / 5).map { (0 until size / 5).map { random.nextInt(10) == 0 } }
            for (i in 0 until size) for (j in 0 until size) map[i][j].rock = map[i][j].rock || hasBlocks[i / 5][j / 5];
        }
    }; val map = Map()

    /**
     * agents
     */
    val agentConfig = configured(AgentConfig()) {  }
    val agents = (0 until playerSize).map {
        configured(Agent()) { config =  agentConfig; position = svec2(0.5F, 0.5F) }
    }

    init {
        for (a in agents) {
            while (true) {
                val t = map.random()
                if (!t.rock) {
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
        for (a in agents) {
            a.walk()
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
        var pos = svec2()
        var tpos = svec2()
        var ipos = ivec2()


        @Strictfp operator fun invoke(position: StrictVector2, dest: IntVector2, /* out */ route: MutableList<MapTile>) {
            if (map(dest).rock) {
                println("glitch: dest is rock")
                return
            }
            route.clear()
            this.counter++
            pq.clear()
            pos.set(position)
            ipos.set(pos)
            run {
                val next = map(ipos)
                next.center(tpos)
                val cost = (pos - tpos).len()
                util_tryAddRoute(next, dest, IntVector2.Zero, cost)
            }
            for (vec in relativeCorners) {
                val next = map(ipos.x + vec.x, ipos.y + vec.y)
                val next0 = map(ipos.x + vec.x, ipos.y)
                val next1 = map(ipos.x, ipos.y + vec.y)
                if (!next.rock && !next0.rock && !next1.rock) {
                    pos.set(position)
                    next.center(tpos)
                    val cost = (pos - tpos).len()
                    util_tryAddRoute(next, dest, vec, cost)
                }
            }
            for (vec in relativeSides) {
                val next = map(ipos.x + vec.x, ipos.y + vec.y)
                if (!next.rock) {
                    pos.set(position)
                    next.center(tpos)
                    val cost = (pos - tpos).len()
                    util_tryAddRoute(next, dest, vec, cost)
                }
            }
            while (!pq.isEmpty()) {
                val nearest = pq.poll()!!
                if (nearest.position == dest) {
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
                    if (!next.rock && !next0.rock && !next1.rock) {
                        util_tryAddRoute(next, dest, vec, nearest.temp_cost + cornerCost)
                    }
                }
                for (vec in relativeSides) {
                    val next = map(ipos.x + vec.x, ipos.y + vec.y)
                    if (!next.rock) {
                        util_tryAddRoute(next, dest, vec, nearest.temp_cost + 1F)
                    }
                }
            }
            return // no route
        }

        val apos = ivec2()

        inline private fun util_tryAddRoute(next: MapTile, dest: IntVector2, vec: IntVector2, cost: Float) {
            val new = next.temp_visited != counter
            if (new || cost < next.temp_cost) {
                if (!new) pq.remove(next)
                next.temp_cost = cost
                apos.set(dest)
                apos - next.position
                val remainingDis = apos.len()
                next.temp_priority = cost + remainingDis
                next.temp_visited = counter
                next.temp_ttpo = vec
                pq.add(next)
            }
        }

    }; val findRoute = FindRouteWrapper()

    class WalkWrapper {

        val pos = svec2()
        @Strictfp operator fun invoke(walker: Walker, /* in-out */ route: MutableList<MapTile>, /* in-out */ position: StrictVector2) {
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


    inner class Agent : Walker() {
        var health: Int = 10
    }

    inner open class Walker {
        lateinit var config: WalkerConfig
        lateinit var position: StrictVector2
        // immediate next tile
        val route = mutableListOf<MapTile>()

        fun findRoute(dest: IntVector2) = findRoute(position, dest, route)

        fun walk() = walk(this, route, position)
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





