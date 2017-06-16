package org.snailya.bnw.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.log.debug
import ktx.math.*
import ktx.scene2d.*
import org.snailya.base.*
import org.snailya.bnw.NetworkingCommon
import org.snailya.bnw.PlayerCommand
import org.snailya.bnw.gamelogic.BnwGame
import org.snailya.bnw.networking.ServerConnection
import java.util.*

/**
 *
 * a normal game loop will be like:
 *
 * get and apply input
 * update game state
 * render
 *
 * a networked game loop will be:
 *
 */



class GamePage(val c: ServerConnection) : Page() {

    val debug_img = Texture("badlogic.jpg")

    val g = BnwGame(c.myIndex, c.playerSize, c.gameStartTime)

    lateinit  var debug_info: Label
    init {
        ui = table {
            debug_info = label("")
            row()
            debug = true
        }
    }

    var focus = g.center.copy()
    var focusSpeed = 1F
    var zoom = 48.dp
    val projection: Matrix4 = Matrix4()
    val inverseProjection : Matrix4 = Matrix4()

    init {
        inputProcessor = object : BaseInputProcessor() {
            override fun scrolled(amount: Int): Boolean {
                zoom *= (1 + amount.dp / 50)
                return true
            }
        }
    }

    fun inputGameCoor(x: Int, y: Int) =
            vec2(x.tf * 2 / game.backBufferWidth() - 1, 1 - y.tf * 2 / game.backBufferHeight()).extends().mul(inverseProjection).lose()


    var dest: Vector2? = null
    val debug_random = Random()

    override fun render() {

        // enqueue game input
        if (!c.gamePaused) {
            if (Gdx.input.justTouched()) {
                dest = inputGameCoor(Gdx.input.x, Gdx.input.y)
            }
        }

        // get previous tick confirmed operations
        // dequeue game input, network tick
        // game simulation tick
        val time = System.currentTimeMillis()
        var gameTicks = 0
        var netTicks = 0
        while (true) {
            val toTime = g.time + NetworkingCommon.timePerGameTick
            if (toTime <= time) {
                gameTicks += 1
                var confirmedCommands: List<List<PlayerCommand>>? = null
                if (c.time + NetworkingCommon.timePerTick == toTime) {
                    netTicks += 1
                    confirmedCommands = c.tick(if(dest == null) emptyList() else listOf(PlayerCommand(dest)), g.debug_hash())
                    if (c.gamePaused) {
                        g.time += NetworkingCommon.timePerTick
                        break
                    } else {
                        dest = null
                    }
                }
                g.tick(confirmedCommands)
            } else {
                break
            }
        }

        if (c.gamePaused) {
            debug_info.setText("PAUSED")
            return
        }
        // info { "game tick $gameTicks, net tick $netTicks" }

        run { // local input
            val delta = Gdx.graphics.deltaTime
            val direction = vec2(0F, 0F)
            run {
                fun keyed(i: Int) = Gdx.input.isKeyPressed(i)
                if (keyed(Input.Keys.W)) direction.add(0F, 1F)
                if (keyed(Input.Keys.S)) direction.add(0F, -1F)
                if (keyed(Input.Keys.A)) direction.add(-1F, 0F)
                if (keyed(Input.Keys.D)) direction.add(1F, 0F)
                direction.nor()
            }
            focus + (direction * (focusSpeed * delta))
        }



        projection.setToOrtho2DCentered(focus.x, focus.y, game.backBufferWidth() / zoom, game.backBufferHeight() / zoom)

        inverseProjection.set(projection)
        inverseProjection.inv()


        val gtl = vec3(-1F, -1F, 0F) * inverseProjection
        val gbr = vec3(1F, 1F, 0F) * inverseProjection

        batch.projectionMatrix = projection

        val margin = 0
        val top = Math.max(gtl.y.toInt() - margin, 0)
        val left = Math.max(gtl.x.toInt() - margin, 0)
        val bottom = Math.min(gbr.y.toInt() + 1 + margin, g.mapSize)
        val right = Math.min(gbr.x.toInt() + 1 + margin, g.mapSize)

        val debugText = "tl: $gtl, br: $gbr, focus: $focus\n" +
                "top $top, left $left, right $right, bottom $bottom"

        debug_info.setText(debugText)

        batch.begin()

//        for (y in top until bottom) {
//            for (x in left until right) {
//                batch.color = Color(g.map[x][y].debug_color)
//                batch.draw(debug_img, x.tf, y.tf, 1F, 1F)
//            }
//        }
        for (agent in g.agents) {
            batch.draw(debug_img, agent.position.x - 0.5F, agent.position.y - 0.5F, 1F, 1F)
        }

        batch.end()
    }


    override fun dispose() {
        debug_img.dispose()
    }

}
