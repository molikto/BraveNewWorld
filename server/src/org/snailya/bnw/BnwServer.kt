package org.snailya.bnw

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.esotericsoftware.minlog.Log
import org.joor.Reflect
import org.snailya.base.tif
import org.snailya.base.timet
import java.lang.reflect.AccessibleObject.setAccessible
import java.lang.reflect.Field


object BnwServer : Listener() {

    lateinit var server: Server

    var gameStartTime: Long = 0
    var tick = 0
    var previousConfirmation: GameCommandsMessage? = null

    const val gameSize = 1
    var cachedCommands: Array<PlayerCommandsMessage?> = emptyArray()

    @JvmStatic fun main(arg: Array<String>) {
        server = NetworkingShared.createServer()
        server.addListener(LagListener(60, 80, this))
        //server.addListener(this)
        server.bind(NetworkingShared.tcpPort, NetworkingShared.udpPort)
        server.start()
    }


    override fun connected(p0: Connection) {
        p0.updateReturnTripTime()
    }

    override fun disconnected(p0: Connection) {
        server.stop()
    }

    override fun idle(p0: Connection) {
    }


    override fun received(c: Connection, p: Any) {
        timet("received form ${c.id} $p") {
            when (p) {
                is FrameworkMessage.Ping -> {
                    if (p.isReply) {
                        val connections = server.connections
                        if (connections.size >= gameSize && connections.all{ it.returnTripTime >= 0 }) {
                            val rtts = connections.map { it.returnTripTime }
                            val maxRtt = rtts.max()!!
                            val maxTick: Int = Math.ceil(maxRtt.toDouble() / NetworkingShared.timePerTick).toInt()
                            tif("RTTs: ${rtts.joinToString(" ")}, maxTick: $maxTick")
                            val time = System.currentTimeMillis()
                            for (cc in connections) {
                                cc.sendTCP(StartGameMessage(indexOf(cc), time, cc.returnTripTime, maxTick, gameSize))
                            }
                            gameStartTime = System.currentTimeMillis()
                            cachedCommands = Array(gameSize, { null })
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
                            for (i in 0 until gameSize) {
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