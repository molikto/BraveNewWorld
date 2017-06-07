package org.snailya.bnw

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.scene2d.*
import ktx.style.color
import ktx.style.label
import ktx.style.skin
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
    }

    init { Scene2DSkin.defaultSkin = defaultSkin }

    init { page = GamePage() }
}

class GamePage : Page() {

    val img = Texture("badlogic.jpg")

    init {
        ui = table {
            label("DEBUG TEXT")
        }
    }

    override fun render() {
        batch.begin()
        batch.draw(img, 0f, 0f)
        batch.end()
    }

    override fun dispose() {
        img.dispose()
    }
}

