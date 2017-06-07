package org.snailya.bnw

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.scene2d.*
import ktx.style.color
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

    val img = Texture("badlogic.jpg")

    var pos = Vector2(0F, 0F)

    init {
        ui = table {
            label("DEBUG TEXT")
            textButton("BUTTON") {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        pos = Vector2(x, y)
                    }
                })
            }
        }
    }

    override fun render() {
        batch.begin()
        batch.draw(img, pos.x, pos.y)
        batch.end()
    }

    override fun dispose() {
        img.dispose()
    }
}

