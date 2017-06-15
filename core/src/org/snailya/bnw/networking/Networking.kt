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
import org.snailya.bnw.NetworkingCommon
import org.snailya.bnw.StartGameMessage


class ServerConnection(val ip: String) {


    private val client: Client = NetworkingCommon.createClient()

    val myId: Int
     get() = client.id

    data class State(
            var rttGot: Boolean = false,
            var gameStartTime: Long = -1L,
            var pendingTick: Int = -1,
            var  playerSize: Int = 0,
            var delay: Int = 1

    ) {
        fun gameStarted() = gameStartTime >= 0
    }

    private val state: BehaviorSubject<State> = BehaviorSubject.createDefault(State())

    fun obs(): Observable<State> = state.observeOn(GdxScheduler)

    val value: State
        get() = state.value

    init {
        client.addListener(object : Listener() {
            override fun connected(connection: Connection) {
            }

            override fun disconnected(connection: Connection) {
            }

            override fun idle(connection: Connection) {
            }

            override fun received(connection: Connection, obj: Any) {
                when (obj) {
                    is FrameworkMessage.Ping -> {
                        if (obj.isReply) {
                            state.value.rttGot = true
                            state.onNext(state.value)
                        }
                    }
                    is StartGameMessage -> {
                        state.value.gameStartTime = System.currentTimeMillis()
                        state.value.pendingTick = 0
                        state.value.playerSize = obj.playerSize
                        state.value.delay = obj.delay
                        state.onNext(state.value)
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
            client.start()
            client.connect(5000, ip, NetworkingCommon.tcpPort, NetworkingCommon.udpPort)
            this
        }.subscribeOn(Schedulers.io()).observeOn(GdxScheduler)
    }
}


