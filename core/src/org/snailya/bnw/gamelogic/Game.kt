package org.snailya.bnw.gamelogic

import com.badlogic.gdx.math.Vector2
import ktx.math.*
import org.snailya.base.configured
import org.snailya.base.copy
import org.snailya.base.tf
import org.snailya.bnw.*
import java.util.*

val debug_random = Random()

open class AgentConfig : WalkerConfig() {
}
class Agent : Walker() {
    var health: Int = 10
}

open class WalkerConfig {
    var speed: Float = 1F
}

open class Walker {
    lateinit var config: WalkerConfig
    lateinit var position: Vector2
    var dest: Vector2? = null
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



class BnwGame(val myIndex: Int, val playerSize: Int, val gameStartTime: Long) {

    val random = Random(gameStartTime)

    val tickTime = NetworkingShared.timePerGameTick
    var tickedTime = gameStartTime
    val mapSize = 300
    val map: Array<Array<MapTile>> = Array(mapSize, { Array(mapSize, { MapTile() })})

    init {
        val hasBlocks = (0 until mapSize / 5).map { (0 until mapSize / 5).map { random.nextBoolean() } }
        for (i in 0 until mapSize) for (j in 0 until mapSize) map[i][j].isBlock = hasBlocks[i / 5][j / 5];
    }
    val center =  vec2(mapSize.tf / 2, mapSize.tf / 2)


    val agentConfig = configured(AgentConfig()) {  }
    val agents = (0 until playerSize).map {
        configured(Agent()) { config =  agentConfig; position = center.copy() }
    }

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
                a.position + (it.copy() - a.position).nor() * tickTime / 1000 * a.config.speed
            }
        }
        tickedTime += tickTime
    }

    fun  debug_hash(): Int {
        return agents.map { it.position.hashCode() }.sum()
    }
}

