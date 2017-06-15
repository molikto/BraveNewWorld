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
import org.snailya.bnw.NetworkingCommon
import org.snailya.bnw.PlayerInput
import org.snailya.bnw.gamelogic.BnwGame
import org.snailya.bnw.networking.ServerConnection

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
            vec2(x.tf, (game.backBufferHeight() -  y).tf).extends().mul(inverseProjection).lose()


    var dest: Vector2? = null

    override fun render() {
        if (Gdx.input.justTouched()) {
            dest = inputGameCoor(Gdx.input.x, Gdx.input.y)
        }

        val time = System.currentTimeMillis()
        while (g.time + NetworkingCommon.timePerSimulation <= time) {
            val toTime = g.time + NetworkingCommon.timePerSimulation
            var inputs: List<List<PlayerInput>>? = null
            if (c.time + NetworkingCommon.timePerTick == toTime) {
                inputs = c.tick(listOf(org.snailya.bnw.PlayerInput(dest)))
                dest = null
            }
            g.tick(inputs)
        }

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
