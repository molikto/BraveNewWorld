package org.snailya.bnw.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.math.*
import ktx.scene2d.*
import org.snailya.base.*
import org.snailya.bnw.PlayerCommand
import org.snailya.bnw.gamelogic.BnwGame
import org.snailya.bnw.networking.ServerConnection
import org.snailya.bnw.timePerGameTick
import org.snailya.bnw.timePerTick

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

    /**
     * textures
     *
     * game textures should be later defined in data files, not here
     */
    val debug_img = Texture("badlogic.jpg")
    val sand = Texture("sand.png")
    val rock = Texture("rock.png")



    /**
     * game simulation
     */
    val g = BnwGame(c.myIndex, c.playerSize, c.gameStartTime)

    /**
     * ui
     */
    lateinit  var debug_info: Label
    init {
        ui = table {
            debug_info = label("")
            row()
            debug = true
        }
    }

    /**
     * game ui
     */
    var focus: Vector2 = g.agents[g.myIndex].position.vec2()
    var focusSpeed = 10F
    var zoom = 48.dp

    init {
        inputProcessor = object : BaseInputProcessor() {
            override fun scrolled(amount: Int): Boolean {
                zoom *= (1 + amount.dp / 50)
                return true
            }
        }
    }

    /**
     * commands
     */
    val commands = mutableListOf<PlayerCommand>()

    /**
     * rendering
     */
    val projection: Matrix4 = Matrix4()
    val inverseProjection : Matrix4 = Matrix4()

    fun inputGameCoor(x: Int, y: Int) =
            vec2(x.tf * 2 / game.backBufferWidth() - 1, 1 - y.tf * 2 / game.backBufferHeight()).extends().mul(inverseProjection).lose()


    /**
     * ticks
     */
    var gameTickedTime = c.gameStartTime
    var networkTickedTime: Long = c.gameStartTime

    override fun render() {

        // if not paused - enqueue game input
        if (!c.gamePaused) {
            if (Gdx.input.justTouched()) {
                val dest = inputGameCoor(Gdx.input.x, Gdx.input.y).ivec2()
                if (!g.map(dest).rock) {
                    commands.add(PlayerCommand(dest))
                }
            }
        }

        // tick the network and game - maybe causing a pause
        val time = System.currentTimeMillis()
        if (c.gamePaused && c.received != null) {
            gameTickedTime = c.receivedTime - timePerGameTick
            networkTickedTime = c.receivedTime - timePerTick
        }
        var gameTicks = 0
        var netTicks = 0
        while (true) {
            val toTime = gameTickedTime + timePerGameTick
            if (toTime <= time) {
                gameTicks += 1
                var confirmedCommands: List<List<PlayerCommand>>? = null
                if (networkTickedTime + timePerTick == toTime) {
                    netTicks += 1
                    confirmedCommands = c.tick(commands, g.debug_hash())
                    networkTickedTime += timePerTick
                    if (c.gamePaused) {
                        // we schedule a resend at next tick time
                        gameTickedTime += timePerTick
                        break
                    } else {
                        commands.clear()
                    }
                }
                g.tick(confirmedCommands)
                gameTickedTime += timePerGameTick
            } else {
                break
            }
        }
        // info { "game tick $gameTicks, net tick $netTicks" }

        // game paused UI, just return
        if (c.gamePaused) {
            debug_info.setText("PAUSED")
            return
        }


        // local input

        run {
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


        /**
         * game rendering
         */

        projection.setToOrtho2DCentered(focus.x, focus.y, game.backBufferWidth() / zoom, game.backBufferHeight() / zoom)

        inverseProjection.set(projection)
        inverseProjection.inv()


        val gtl = vec3(-1F, -1F, 0F) * inverseProjection
        val gbr = vec3(1F, 1F, 0F) * inverseProjection

        batch.projectionMatrix = projection

        val margin = 0
        val top = Math.max(gtl.y.toInt() - margin, 0)
        val left = Math.max(gtl.x.toInt() - margin, 0)
        val bottom = Math.min(gbr.y.toInt() + 1 + margin, g.map.size)
        val right = Math.min(gbr.x.toInt() + 1 + margin, g.map.size)

        val debugText = "tl: $gtl, br: $gbr, focus: $focus\n" +
                "top $top, left $left, right $right, bottom $bottom"

        debug_info.setText(debugText)

        batch.begin()

        for (y in top until bottom) {
            for (x in left until right) {
                batch.draw(if (g.map(x, y).rock) rock else sand, x.tf, y.tf, 1F, 1F)
            }
        }
        // TODO maybe I need a different shader for the background and moving things, or different projection..
        for (agent in g.agents) {
            batch.draw(debug_img, agent.position.x - 0.5F, agent.position.y - 0.5F, 1F, 1F)
        }

        batch.end()
    }


    override fun dispose() {
        debug_img.dispose()
    }

}


