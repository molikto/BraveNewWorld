package org.snailya.bnw

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import ktx.scene2d.*
import ktx.style.label
import ktx.style.skin
import ktx.style.textButton
import org.snailya.base.*

/**
 * BASIC SETUP
 */
class BraveNewWorldWrapper(pdi: PlatformDependentInfo) : ApplicationWrapper({ BraveNewWorld(pdi) })

var _bnw: BraveNewWorld? = null
val bnw by lazy { _bnw!! }

/**
 * REAL THING
 */

class BraveNewWorld(pdi: PlatformDependentInfo) : ApplicationInner(pdi) {
    init { _bnw = this  }

    val RobotoMono = fontGenerator("fonts/RobotoMono-Regular.ttf")
    val RototoMono14 = RobotoMono.ofSize(14.dp)

    val defaultSkin = skin {
        label {
            font = RototoMono14
            fontColor = Color.WHITE
        }
        textButton {
            font = RototoMono14
            fontColor = Color.WHITE
        }
    }

    init { Scene2DSkin.defaultSkin = defaultSkin }

    init { page = GamePage() }
}

class GamePage : Page() {

    val debug_img = Texture("badlogic.jpg")

    var debug_pos = Vector2(0F, 0F)

    init {
        ui = table {
            label("DEBUG TEXT")
            row()

            textButton("DEBUG BUTTON") {
                onClick { event, _, _ ->
                    debug_pos = Vector2(event.stageX, event.stageY)
                }
            }
            debug = true
        }
    }

    override fun render() {
//        batch.begin()
//        batch.draw(debug_img, debug_pos.x, debug_pos.y)
//        batch.end()
    }

    override fun dispose() {
        debug_img.dispose()
    }
}

