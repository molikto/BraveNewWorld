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

class MapTile {
    var debug_color: Int = debug_random.nextInt()
}



class BnwGame(val myIndex: Int, val playerSize: Int, val gameStartTime: Long) {

    val tickTime = NetworkingCommon.timePerSimulation
    var time = gameStartTime
    val mapSize = 300
    val map: Array<Array<MapTile>> = Array(mapSize, { Array(mapSize, { MapTile() })})
    val center =  vec2(mapSize.tf / 2, mapSize.tf / 2)

    val agentConfig = configured(AgentConfig()) {  }
    val agents = (0 until playerSize).map {
        configured(Agent()) { config =  agentConfig; position = center.copy() }
    }

    fun tick(inputs: List<List<PlayerInput>>?) {
        if (inputs != null) {
            assert(inputs.size == playerSize)
            for (i in 0 until playerSize) {
                val agent = agents[i]
                val input = inputs[i]
                for (j in input) {
                    agent.dest = j.dest
                }
            }
        }
        for (a in agents) {
            a.dest?.let {
                a.position + (it.copy() - a.position).nor() * tickTime / 1000 * a.config.speed
            }
        }
        time += tickTime
    }
}

