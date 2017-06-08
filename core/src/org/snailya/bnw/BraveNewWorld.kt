package org.snailya.bnw

import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import ktx.scene2d.*
import ktx.style.label
import ktx.style.skin
import ktx.style.textButton
import org.snailya.base.*

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

    val game = Game()

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

    override fun render() {
        val time = graphics.deltaTime
        run {
            fun keyed(i: Int) = input.isKeyPressed(i)
            val direction = Vector2(0F, 0F)
            if (keyed(Input.Keys.W)) direction.add(0F, 1F)
            if (keyed(Input.Keys.S)) direction.add(0F, -1F)
            if (keyed(Input.Keys.A)) direction.add(-1F, 0F)
            if (keyed(Input.Keys.D)) direction.add(1F, 0F)
            game.move(direction, time)
        }
        batch.begin()
        batch.draw(debug_img, game.position)
        batch.end()
    }

    override fun dispose() {
        debug_img.dispose()
    }
}

