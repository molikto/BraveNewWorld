package org.snailya.bnw.gamelogic

import com.badlogic.gdx.math.Vector2
import ktx.math.*
import org.snailya.base.configured
import org.snailya.base.copy
import org.snailya.base.tf
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
}

class MapTile {
    var debug_color: Int = debug_random.nextInt()
}



class BnwGame(val myId: Int, val playerSize: Int) {
    val mapSize = 300
    val map: Array<Array<MapTile>> = Array(mapSize, { Array(mapSize, { MapTile() })})
    val center =  vec2(mapSize.tf / 2, mapSize.tf / 2)

    val agentConfig = configured(AgentConfig()) {  }
    val agent = configured(Agent()) { config =  agentConfig; position = center.copy() }

//    fun move(direction: Vector2, time: Float) {
//        player.position + (direction * (player.config.speed * time))
//    }


    fun step(inputs: Array<PlayerInput>) {
    }
}

class PlayerInfo(val id: Int) {
}

class PlayerState(val info: PlayerInfo) {
}

class PlayerInput() {
}
