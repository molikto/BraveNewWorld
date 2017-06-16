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
import org.snailya.base.post
import org.snailya.bnw.*


class ServerConnection(val ip: String) {


    private val client: Client = NetworkingCommon.createClient()

    // valid after ping got
    var rttGot: Boolean = false

    // valid after game started
    var myIndex: Int = 0
    var gameStartTime: Long = -1L
    var playerSize: Int = 0
    var delay: Int = 1

    // game states that changes
    // gameStartTime + tick * timePerTick = time
    var time: Long = -1L
    var tick = 0



    var received: GameCommandsMessage? = null

    fun  tick(commands: List<PlayerCommand>): List<List<PlayerCommand>>? {
        tick += 1
        time += NetworkingCommon.timePerTick
        client.sendUDP(PlayerCommandsMessage(tick - 1, commands))
        if (tick > 1) {
            // TODO lost connection
            val res = received!!.commands
            received = null
            return res
        } else {
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
                post {
                    info { "received $obj, $tick" }
                    when (obj) {
                        is FrameworkMessage.Ping -> {
                            if (obj.isReply) {
                                rttGot = true
                                state.onNext(Unit)
                            }
                        }
                        is StartGameMessage -> {
                            gameStartTime = System.currentTimeMillis()
                            time = gameStartTime
                            myIndex = obj.myIndex
                            playerSize = obj.playerSize
                            delay = obj.delay
                            state.onNext(Unit)
                        }
                        is GameCommandsMessage -> {
                            if (obj.tick == tick - 1) {
                                received = obj
                            } else {
                                throw Exception("$obj, $tick")
                            }
                        }
                    }
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
            client.connect(5000, ip, NetworkingCommon.tcpPort, NetworkingCommon.udpPort)
            info { "connected to remote" }
            this
        }.subscribeOn(Schedulers.io()).observeOn(GdxScheduler)
    }

}


