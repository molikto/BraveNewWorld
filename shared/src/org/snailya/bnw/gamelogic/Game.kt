package org.snailya.bnw.gamelogic

import org.snailya.base.*
import org.snailya.base.tf
import org.snailya.bnw.*
import java.util.*
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus


open class AgentConfig : WalkerConfig() {
}
class Agent : Walker() {
    var health: Int = 10
}

open class WalkerConfig {
    var speed: Float = 0.02F /* per tick */
}

open class Walker {
    lateinit var config: WalkerConfig
    lateinit var position: StrictVector2
    var dest: StrictVector2? = null
}


/**
 * https://en.wikipedia.org/wiki/Soil
 */
enum class GroundType {
    Snow, Sand, Soil, SoilRich,
//    Snow, Tundra, Bare, Scorched,
//    Taiga, Shrubland, TemperateDesert,
//    TemperateRainForest, TemperateDec, Grassland,
//    TropicalRainForest, TropicalSeasonalForest, SubtropicalDesert

}
class MapTile {
    var isBlock = false
}


class BnwGame(val myIndex: Int, val playerSize: Int, seed: Long) {

    /**
     * basic
     */
    val random = Random(seed)

    /**
     * map
     */
    val mapSize = 100
    val map: Array<Array<MapTile>> = Array(mapSize, { Array(mapSize, { MapTile() })})


    init { // temp generate map and players
        val hasBlocks = (0 until mapSize / 5).map { (0 until mapSize / 5).map { random.nextInt(10) == 0 } }
        for (i in 0 until mapSize) for (j in 0 until mapSize) map[i][j].isBlock = hasBlocks[i / 5][j / 5];
    }

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
                val x = random.nextInt(mapSize)
                val y = random.nextInt(mapSize)
                if (!map[x][y].isBlock) {
                    a.position = svec2(x + 0.5F, y + 0.5F)
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
                for (c in cs) {
                    agent.dest = c.dest
                }
            }
        }
        for (a in agents) {
            a.dest?.let {
                a.position + (it.copy() - a.position).nor() * a.config.speed
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





