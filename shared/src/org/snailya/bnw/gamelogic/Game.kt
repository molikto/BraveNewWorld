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
    var temp_visited: Int = -1
    var temp_ttpo: IntVector2 = IntVector2.Zero // to the previous of
}

val mapCorners = arrayOf(ivec2(-1, -1), ivec2(1, -1), ivec2(-1, 1), ivec2(1, 1), ivec2(-1, 0), ivec2(1, 0), ivec2(0, 1), ivec2(0, -1))
val cornerCost = StrictMath.sqrt(2.0).toFloat()
val mapCornerCosts = floatArrayOf(cornerCost, cornerCost, cornerCost, cornerCost, 1F, 1F, 1F, 1F)


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
            for (i in 0 until size) for (j in 0 until size) map[i][j].rock = hasBlocks[i / 5][j / 5];
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

    inner class FindRouteWrapper {
        // all variables is temp
        var counter = -1
        val pq = PriorityQueue<MapTile>(30, object : Comparator<MapTile> {

            @Strictfp
            override fun compare(o1: MapTile, o2: MapTile): Int {
                if (o1.temp_cost > o2.temp_cost) {
                    return 1
                } else if (o1.temp_cost < o2.temp_cost) {
                    return -1
                }
                return 0
            }
        })
        var pos = svec2()
        var tpos = svec2()
        var ipos = ivec2()


        @Strictfp operator fun invoke(position: StrictVector2, dest: IntVector2, /* out */ route: MutableList<MapTile>) {
            if (!map(dest).rock) {
                route.clear()
            } else {
                println("glitch: dest is rock")
                return
            }
            this.counter++
            val counter = this.counter
            pq.clear()
            pos.set(position)
            ipos.set(pos)
            for (x in (ipos.x - 1) .. (ipos.x + 1)) {
                for (y in (ipos.y - 1) .. (ipos.y + 1)) {
                    val tile = map(x, y)
                    if (!tile.rock) {
                        pos.set(position)
                        tile.center(tpos)
                        tile.temp_cost = (pos - tpos).len()
                        tile.temp_visited = counter
                        tile.temp_ttpo = IntVector2.Zero
                        pq.add(tile)
                    }
                }
            }
            while (!pq.isEmpty()) {
                val nearest = pq.poll()!!
                if (nearest.position == dest) {
                    var t = nearest
                    do {
                        route.add(t)
                        t = map(nearest.position.x - nearest.temp_ttpo.x, nearest.position.y - nearest.temp_ttpo.y)
                    } while (t.temp_ttpo != IntVector2.Zero)
                    return
                }
                ipos.set(nearest.position)
                for (i in 0 until 8) {
                    val vec = mapCorners[i]
                    val next = map(ipos.x + vec.x, ipos.y + vec.y)
                    val cost = nearest.temp_cost + mapCornerCosts[i]
                    val new = next.temp_visited != counter
                    if (new || cost < next.temp_cost) {
                        if (!new) pq.remove(next)
                        next.temp_cost = cost
                        next.temp_visited = counter
                        next.temp_ttpo = vec
                        pq.add(next)
                    }
                }
            }
            return // no route
        }

    }; val findRoute = FindRouteWrapper()

    class WalkWrapper {

        @Strictfp operator fun invoke(route: MutableList<MapTile>, /* in-out */ position: StrictVector2) {
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

        fun walk() = walk(route, position)
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





