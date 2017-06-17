package org.snailya.bnw.networking

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ktx.log.info
import org.snailya.base.GdxScheduler
import org.snailya.base.*
import org.snailya.bnw.*


class ServerConnection(val ip: String) {


    private val client: Client = NetworkingShared.createClient()

    var rttGot: Boolean = false

    val id
        get() = client.id
    // valid after game started
    var myIndex: Int = 0
    var gameStartTime: Long = -1L
    var playerSize: Int = 0
    var delay: Int = 1

    var tickedTime: Long = -1L
    var tick = 0
    var previousCommands: PlayerCommandsMessage? = null
    var gamePaused: Boolean = false

    var continuousPausedFrames = 0
    var pausedFrames = 0



    var received: GameCommandsMessage? = null
    var receivedTime: Long = 0L

    var debug_previousSendTime = 0L
    var debug_previousSendTick = 0

    fun  tick(commands: List<PlayerCommand>, debug_hash: Int): List<List<PlayerCommand>>? {
        val isFirstTick = tick == 0
        if (isFirstTick || received != null) {
            val message = PlayerCommandsMessage(tick, debug_hash, commands, false)
            previousCommands = message.copy(debug_resend = true)
            debug_previousSendTime = System.currentTimeMillis()
            debug_previousSendTick = tick
            time("sending message $tick") { client.sendUDP(message) }
            gamePaused = false
            continuousPausedFrames = 0
            tickedTime += NetworkingShared.timePerTick
            tick += 1
            if (isFirstTick) {
                return null
            } else {
                val res = received!!.commands
                received = null
                receivedTime = 0L
                return res
            }
        } else {
            val tick = previousCommands!!.tick
            debug_previousSendTime = System.currentTimeMillis()
            debug_previousSendTick = tick
            time("resending message $tick") { client.sendUDP(previousCommands) }
            gamePaused = true
            pausedFrames += 1
            continuousPausedFrames += 1
            // will ignore the the input, also the output should not be used
            return null
        }
    }

    private val state: BehaviorSubject<Unit> = BehaviorSubject.createDefault(Unit)

    fun obs(): Observable<Unit> = state

    init {
        client.addListener(object : Listener() {
            override fun connected(connection: Connection) {
            }

            override fun disconnected(connection: Connection) {
            }

            override fun idle(connection: Connection) {
            }

            override fun received(connection: Connection, obj: Any) {
                tif("received called in network thread")
                post {
                    var debug_unexpected = false
                    when (obj) {
                        is FrameworkMessage.Ping -> {
                            rttGot = true
                            state.onNext(Unit)
                        }
                        is StartGameMessage -> {
                            gameStartTime = System.currentTimeMillis() + 50
                            tickedTime = gameStartTime
                            myIndex = obj.myIndex
                            playerSize = obj.playerSize
                            delay = obj.delay
                            state.onNext(Unit)
                        }
                        is GameCommandsMessage -> {
                            if (obj.tick == tick - 1) {
                                received = obj
                                receivedTime = System.currentTimeMillis()
                            } else {
                                debug_unexpected = true
                                tif("unexpected message $obj, $tick")
                            }
                        }
                    }
                    if (!debug_unexpected) tif("received $obj, $tick")
                }
            }
        })
    }


    fun disconnect() {
        client.dispose()
    }

    fun connect(): Single<ServerConnection> {
        return Single.fromCallable {
            info { "connecting to remote" }
            client.start()
            client.connect(5000, ip, NetworkingShared.tcpPort, NetworkingShared.udpPort)
            info { "connected to remote" }
            this
        }.subscribeOn(Schedulers.io()).observeOn(GdxScheduler)
    }

}


