package org.snailya.bnw

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server

/**
 * Created by molikto on 14/06/2017.
 */


object BnwServer {

    @JvmStatic fun main(arg: Array<String>) {
        val server = Server()
        NetworkingCommon.register(server)

        var gameStartTime = -1L

        server.addListener(object : Listener() {
            override fun connected(p0: Connection) {
                p0.updateReturnTripTime()
            }

            override fun disconnected(p0: Connection) {
            }

            override fun idle(p0: Connection) {
            }

            override fun received(p0: Connection, p1: Any) {
                when (p1) {
                    is FrameworkMessage.Ping -> {
                        val connections = server.connections
                        if (connections.size >= 2 && connections.all{ it.returnTripTime >= 0 }) {
                            val rtts = connections.map { it.returnTripTime }
                            val maxRtt = rtts.max()!!
                            val maxTick: Int = Math.ceil(maxRtt.toDouble() / NetworkingCommon.timePerTick).toInt()
                            println("RTTs: ${rtts.joinToString(" ")}, maxTick: $maxTick")
                            for (c in connections) {
                                c.sendTCP(StartGameMessage(maxTick))
                            }
                            gameStartTime = System.currentTimeMillis()
                        }
                    }
                }
            }
        })
        server.bind(NetworkingCommon.tcpPort, NetworkingCommon.udpPort)
        server.start()
    }

}