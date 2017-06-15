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


class ServerConnection(internal val client: Client) {

    data class State(val rttGot: Boolean = false, val tick: Int = -1) {
        fun gameStarted() = tick >= 0
    }

    private val state: BehaviorSubject<State> = BehaviorSubject.createDefault(State())

    fun obs(): Observable<State> = state.observeOn(GdxScheduler)

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
                    is FrameworkMessage.Ping ->
                            if (obj.isReply) state.onNext(State(true))
                    is StartGameMessage -> {
                        state.onNext(state.value.copy(tick = 0))
                    }
                }
            }
        })
    }


    fun disconnect() {
        client.dispose()
    }
}
class Networking {


    /**
     * op: if success, gameSession will have a value
     */
    fun join(ip: String): Single<ServerConnection> =
        Single.fromCallable {
            val connected = ServerConnection(NetworkingCommon.createClient())
            connected.client.start()
            connected.client.connect(5000, ip, NetworkingCommon.tcpPort, NetworkingCommon.udpPort)
            connected
        }.subscribeOn(Schedulers.io()).observeOn(GdxScheduler)
}
