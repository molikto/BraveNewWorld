package org.snailya.bnw.ui

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
        post {
            joiningServer("0.0.0.0")
        }
    }

    fun onErrorGoBack(c: ServerConnection?) {
        c?.disconnect()
        bnw.change { JoinServerPage() }
    }

    fun joiningServer(ip: String) = simplePage {
        bnw.net.join(ip).flatMap {
            val connection = it
            it.obs().filter{it.rttGot}.firstOrError().map { connection } }.subscribe({ c ->
            bnw.change { waitingForGame(c) }
        }, {
            onErrorGoBack(null)
        })
        table {
            label("joining server at $ip")
        }
    }

    fun waitingForGame(c: ServerConnection) = simplePage {
        c.obs().filter { it.gameStarted() }.firstOrError().subscribe({
            bnw.change { tempInGame() }
        }, {
            onErrorGoBack(c)
        })
        table {
            label("waiting for game")
        }
    }

    private fun  tempInGame() = simplePage {
        table { label("in game") }
    }

}
