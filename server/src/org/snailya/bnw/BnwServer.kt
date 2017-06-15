package org.snailya.bnw

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server

/**
 * Created by molikto on 14/06/2017.
 */


object BnwServer : Listener() {

    lateinit var server: Server

    var gameStartTime: Long = 0
    var tick = 0

    const val gameSize = 2
    var cachedInputs: Array<List<PlayerInput>?> = emptyArray()

    @JvmStatic fun main(arg: Array<String>) {
        server = NetworkingCommon.createServer()
        server.addListener(this)
        server.bind(NetworkingCommon.tcpPort, NetworkingCommon.udpPort)
        server.start()
    }


    override fun connected(p0: Connection) {
        p0.updateReturnTripTime()
    }

    override fun disconnected(p0: Connection) {
    }

    override fun idle(p0: Connection) {
    }


    override fun received(c: Connection, p: Any) {
        println(p)
        when (p) {
            is FrameworkMessage.Ping -> {
                val connections = server.connections
                if (connections.size >= gameSize && connections.all{ it.returnTripTime >= 0 }) {
                    val rtts = connections.map { it.returnTripTime }
                    val maxRtt = rtts.max()!!
                    val maxTick: Int = Math.ceil(maxRtt.toDouble() / NetworkingCommon.timePerTick).toInt()
                    println("RTTs: ${rtts.joinToString(" ")}, maxTick: $maxTick")
                    for (c in connections) {
                        c.sendTCP(StartGameMessage(indexOf(c), maxTick, 2))
                    }
                    gameStartTime = System.currentTimeMillis()
                    cachedInputs = Array(gameSize, { null })
                }
            }
            is PlayerInputMessage -> {
                if (p.tick == tick) {
                    val index = indexOf(c)
                    if (cachedInputs[index] == null) {
                        cachedInputs[index] = p.inputs
                    }
                    if (cachedInputs.all { it != null }) {
                        tick += 1
                        val info = PlayerInputsMessage(tick - 1, cachedInputs.map { it!! }.toList())
                        for (i in 0 until gameSize) {
                            cachedInputs[i] = null
                        }
                        val connections = server.connections
                        for (k in connections) {
                            k.sendUDP(info)
                        }
                    }
                } else {
                    throw Exception("Not current tick")
                }
            }
        }
    }

    private fun  indexOf(c: Connection): Int {
        val cs = server.connections
        var i = 0
        for (cc in cs) {
            if (cc === c) {
                return i
            }
            i+= 1
        }
        return -1
    }


}