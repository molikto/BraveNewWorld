package org.snailya.bnw

import com.esotericsoftware.kryonet.EndPoint

/**
 * Created by molikto on 14/06/2017.
 */

data class StartGameMessage(
        @JvmField val delay: Int
)

class Tick {
}

object NetworkingCommon {
    const val tcpPort = 54554
    const val udpPort = 54552
    const val timePerTick = 100

    fun register(part: EndPoint) {
        val k = part.kryo
        k.register(StartGameMessage::class.java)
    }
}

