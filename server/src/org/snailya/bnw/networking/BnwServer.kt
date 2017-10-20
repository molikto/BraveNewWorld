package org.snailya.bnw.networking

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import org.snailya.base.logging.info
import org.snailya.base.logging.timedResult
import org.snailya.bnw.data.GameCommandsMessage
import org.snailya.bnw.data.PlayerCommandsMessage
import org.snailya.bnw.data.StartGameMessage
import org.snailya.bnw.timePerTick


/**
 * this is currently used for debug purpose
 * a desktop instance will host a server itself in the future...
 * but currently the server is a standalone app
 * this gives the server a simple looping behaviour
 */
object BnwLoopingServer : (() -> Unit) {

    override fun invoke(): Unit {
        BnwGameServer(this)
    }


    @JvmStatic fun main(arg: Array<String>) {
        invoke()
    }
}

class BnwGameServer(val onStop: () -> Unit): Listener() {

    val gamePlayerSize = 1

    var server = createServer()
    init {
        //server.addListener(LagListener(60, 80, this))
        server.addListener(this)
        server.bind(tcpPort, udpPort)
        server.start()
    }

    var gameStartTime: Long = 0
    var tick = 0
    var previousConfirmation: GameCommandsMessage? = null

    var cachedCommands: Array<PlayerCommandsMessage?> = emptyArray()



    override fun connected(p0: Connection) {
        p0.updateReturnTripTime()
    }

    override fun disconnected(p0: Connection) {
        server.stop()
        onStop.invoke()
    }

    override fun idle(p0: Connection) {
    }


    override fun received(c: Connection, p: Any) {
        timedResult("received form ${c.id} $p") {
            when (p) {
                is FrameworkMessage.Ping -> {
                    if (p.isReply) {
                        val connections = server.connections
                        if (connections.size >= gamePlayerSize && connections.all { it.returnTripTime >= 0 }) {
                            val rtts = connections.map { it.returnTripTime }
                            val maxRtt = rtts.max()!!
                            val maxTick: Int = Math.ceil(maxRtt.toDouble() / timePerTick).toInt()
                            info { "RTTs: ${rtts.joinToString(" ")}, maxTick: $maxTick" }
                            val time = System.currentTimeMillis()
                            for (cc in connections) {
                                cc.sendTCP(StartGameMessage(indexOf(cc), time, cc.returnTripTime, maxTick, gamePlayerSize))
                            }
                            gameStartTime = System.currentTimeMillis()
                            cachedCommands = Array(gamePlayerSize, { null })
                        }
                    }
                    ""
                }
                is PlayerCommandsMessage -> {
                    if (p.tick == tick) {
                        val index = indexOf(c)
                        if (cachedCommands[index] == null) {
                            cachedCommands[index] = p
                        }
                        if (cachedCommands.all { it != null }) {
                            tick += 1
                            if (cachedCommands.map { it!!.debug_hash }.toSet().size != 1) {
                                throw Exception("de-sync!!")
                            }
                            val commands = cachedCommands.map { it!!.commands }.toList()
                            val info = GameCommandsMessage(tick - 1, commands, false)
                            previousConfirmation = info.copy(debug_resend = true)
                            for (i in 0 until gamePlayerSize) {
                                cachedCommands[i] = null
                            }
                            val connections = server.connections
                            for (k in connections) {
                                k.sendUDP(info)
                            }
                        }
                        "on time"
                    } else if (p.tick == tick - 1) {
                        val index = indexOf(c)
                        val resend = cachedCommands[index] == null
                        if (resend) c.sendUDP(previousConfirmation)
                        "previous tick, resending $resend"
                    } else if (p.tick > tick) {
                        // ignore
                        "future tick"
                    } else {
                        "very old tick!!"
                    }
                }
                else -> ""
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