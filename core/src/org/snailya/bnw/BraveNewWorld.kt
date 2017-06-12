package org.snailya.bnw

import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.F
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Matrix4.inv
import com.badlogic.gdx.math.Vector2
import com.oracle.util.Checksums.update
import ktx.log.*
import ktx.scene2d.*
import ktx.math.*
import ktx.style.*
import org.snailya.base.*
import org.snailya.bnw.gamelogic.BnwGame
import java.lang.Math.max
import java.lang.Math.min

/**
 * BASIC SETUP
 */
class BraveNewWorldWrapper(pdi: PlatformDependentInfo) : ApplicationWrapper({ BraveNewWorld(pdi) })

val bnw by lazy { game as BraveNewWorld }

/**
 * REAL THING
 */

class BraveNewWorld(pdi: PlatformDependentInfo) : ApplicationInner(pdi) {

    val RobotoMono = fontGenerator("RobotoMono-Regular")
    val RobotoMono14 = RobotoMono.ofSize(14.dp)

    val defaultSkin = skin {
        label {
            font = RobotoMono14
            fontColor = Color.WHITE
        }
        textButton {
            font = RobotoMono14
            fontColor = Color.WHITE
        }
    }

    init { Scene2DSkin.defaultSkin = defaultSkin }

    init { page = GamePage() }
}

class GamePage : Page() {

    val debug_img = Texture("badlogic.jpg")

    val g = BnwGame()

    init {
        ui = table {
            label("DEBUG TEXT")
            row()

            textButton("DEBUG BUTTON") {
                onClick { event, _, _ ->
                }
            }
            debug = true
        }
    }

    var focus = g.center.copy()
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


    override fun render() {
        val time = graphics.deltaTime
        val direction = vec2(0F, 0F)
        run {
            fun keyed(i: Int) = input.isKeyPressed(i)
            if (keyed(Input.Keys.W)) direction.add(0F, 1F)
            if (keyed(Input.Keys.S)) direction.add(0F, -1F)
            if (keyed(Input.Keys.A)) direction.add(-1F, 0F)
            if (keyed(Input.Keys.D)) direction.add(1F, 0F)
            direction.nor()
        }
        // THIS IS THE ONLY
        if (direction.isZero) {
            if (input.isTouched) {
                // order matters!
                direction.set(inputGameCoor(input.x, input.y)- g.player.position).nor()
            }
        }
        g.move(direction, time)

        projection.setToOrtho2DCentered(focus.x, focus.y, game.backBufferWidth() / zoom, game.backBufferHeight() / zoom)

        inverseProjection.set(projection)
        inverseProjection.inv()


        val gtl = vec3(0F, 0F, 0F).mul(inverseProjection)
        val gbr = vec3(1F, 1F, 0F).mul(inverseProjection)

        batch.projectionMatrix = projection

        batch.begin()

        val margin = 3
        val top = max(gtl.y.toInt() - margin, 0)
        val left = max(gtl.x.toInt() - margin, 0)
        val bottom = min(gbr.y.toInt() + margin + 1, 0)
        val right = min(gbr.x.toInt() + margin + 1, 0)

        for (y in top until bottom + 1) {
            for (x in left until right + 1) {
                //batch.draw(debug_img, )
            }
        }
        batch.draw(debug_img, g.player.position.x, g.player.position.y, 1F, 1F)
        batch.end()
    }


    override fun dispose() {
        debug_img.dispose()
    }
}

