package org.snailya.bnw

import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.scenes.scene2d.ui.Label
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

    lateinit  var debug_info: Label
    init {
        ui = table {
            debug_info = label("DEBUG TEXT")
            row()

//            textButton("DEBUG BUTTON") {
//                onClick { event, _, _ ->
//                }
//            }
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
        if (direction.isZero) {
            if (input.isTouched) {
                direction.set(
                        (inputGameCoor(input.x, input.y) -
                                inputGameCoor(game.backBufferWidth() / 2, game.backBufferHeight() / 2)).nor()
                )
            }
        }


        focus + (direction * (focusSpeed * time))
        //g.move(direction, time)

        projection.setToOrtho2DCentered(focus.x, focus.y, game.backBufferWidth() / zoom, game.backBufferHeight() / zoom)

        inverseProjection.set(projection)
        inverseProjection.inv()


        val gtl = vec3(-1F, -1F, 0F) * inverseProjection
        val gbr = vec3(1F, 1F, 0F) * inverseProjection

        batch.projectionMatrix = projection

        val margin = 0
        val top = max(gtl.y.toInt() - margin, 0)
        val left = max(gtl.x.toInt() - margin, 0)
        val bottom = min(gbr.y.toInt() + 1 + margin, g.mapSize)
        val right = min(gbr.x.toInt() + 1 + margin, g.mapSize)

        val debugText = "tl: $gtl, br: $gbr, focus: $focus\n" +
                "top $top, left $left, right $right, bottom $bottom"

        debug_info.setText(debugText)

        batch.begin()

        for (y in top until bottom) {
            for (x in left until right) {
                batch.color = Color(g.map[x][y].debug_color)
                batch.draw(debug_img, x.tf, y.tf, 1F, 1F)
            }
        }
        batch.draw(debug_img, g.player.position.x, g.player.position.y, 1F, 1F)

        batch.end()
    }


    override fun dispose() {
        debug_img.dispose()
    }
}

