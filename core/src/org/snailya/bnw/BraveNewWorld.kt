package org.snailya.bnw

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import org.snailya.base.ApplicationInner
import org.snailya.base.ApplicationWrapper
import org.snailya.base.Page
import org.snailya.base.PlatformDependentInfo

class BraveNewWorldWrapper(pdi: PlatformDependentInfo) : ApplicationWrapper({ BraveNewWorld(pdi) })

var _bnw: BraveNewWorld? = null
val bnw by lazy { _bnw!! }

class BraveNewWorld(pdi: PlatformDependentInfo) : ApplicationInner(pdi) {

    init {
        _bnw = this
        page = GamePage()
    }

    val batch = SpriteBatch()

    override fun dispose() {
        super.dispose()
        batch.dispose()
    }
}

class GamePage : Page() {

    val img = Texture("badlogic.jpg")

    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        bnw.batch.begin()
        bnw.batch.draw(img, 0f, 0f)
        bnw.batch.end()
    }

    override fun dispose() {
        img.dispose()
    }
}

