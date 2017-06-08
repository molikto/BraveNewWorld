package org.snailya.bnw

import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import ktx.scene2d.*
import ktx.math.*
import ktx.style.*
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

    val g = Game()

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
                // order matters!
                direction.set(vec2(input.x.tf, (game.backBufferHeight() -  input.y).tf).gameCoor() - g.position).nor()
            }
        }
        debug("Moving to $direction")
        g.move(direction, time)
        batch.begin()
        val pos = g.position.screenCoor()
        batch.draw(debug_img, pos.x, pos.y)
        batch.end()
    }

    fun Vector2.screenCoor(): Vector2 = this.copy() * 48.dp
    fun Vector2.gameCoor(): Vector2 = this.copy() / 48.dp
    fun Vector2.screenCoor(temp: Vector2): Vector2 = temp.set(this) * 48.dp
    fun Vector2.gameCoor(temp: Vector2): Vector2 = temp.set(this) / 48.dp

    override fun dispose() {
        debug_img.dispose()
    }
}

