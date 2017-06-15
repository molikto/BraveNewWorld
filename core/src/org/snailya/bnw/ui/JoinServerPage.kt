package org.snailya.bnw.ui

import ktx.log.info
import ktx.scene2d.*
import org.snailya.base.*
import org.snailya.bnw.bnw
import org.snailya.bnw.networking.Networking
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
                val ip = ip.text
                bnw.change { joiningServer(ip) }
            }
        }
        post {
            joiningServer("0.0.0.0")
        }
    }

    fun joiningServer(ip: String) = simplePage {
        bnw.net.join(ip).subscribe({ c ->
            bnw.change { waitingForGame(c) }
        }, {
            bnw.change { JoinServerPage() }
        })
        table {
            label("joining server at $ip")
        }
    }

    fun waitingForGame(c: ServerConnection) = simplePage {
        c.obs().subscribe {
            if (it.gameStarted()) bnw.change { tempInGame() }
        }
        table {
            label("waiting for game")
        }
    }

    private fun  tempInGame() = simplePage {
        table { label("in game") }
    }

}
