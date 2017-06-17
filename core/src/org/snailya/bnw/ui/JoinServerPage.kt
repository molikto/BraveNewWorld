package org.snailya.bnw.ui

import com.esotericsoftware.kryonet.Server
import ktx.scene2d.*
import org.snailya.base.*
import org.snailya.bnw.bnw
import org.snailya.bnw.networking.ServerConnection

/**
 * Created by molikto on 14/06/2017.
 */

class JoinServerPage : Page() {

    init {
        ui = table {
            label("put server IP bellow")
            row()

            val ip = textField()
            row()

            textButton("confirm").onClick { _, _, _ ->
                val ipStr = ip.text
                bnw.change { joiningServer(ipStr) }
            }
        }
//        post {
//            joiningServer("")
//        }
    }

    fun onErrorGoBack(err: Throwable, c: ServerConnection?) {
        err.printStackTrace()
        c?.disconnect()
        bnw.change { JoinServerPage() }
    }

    fun joiningServer(ip: String) = simplePage {
        val connection = ServerConnection(ip)
        connection.connect().flatMap {
            it.obs().filter{ connection.rttGot }.firstOrError().map { connection }
        }.subscribe({ c ->
            bnw.change { waitingForGame(c) }
        }, { err ->
            onErrorGoBack(err, null)
        })
        table {
            label("joining server at $ip")
        }
    }

    fun waitingForGame(c: ServerConnection) = simplePage {
        c.obs().filter { c.gameStartTime >= 0 }.firstOrError().subscribe({
            bnw.change { GamePage(c) }
        }, { err ->
            onErrorGoBack(err, c)
        })
        table {
            label("waiting for game")
        }
    }

}
