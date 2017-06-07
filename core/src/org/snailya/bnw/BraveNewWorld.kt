package org.snailya.bnw

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import org.snailya.base.ApplicationInner
import org.snailya.base.ApplicationWrapper
import org.snailya.base.PlatformDependentInfo

class BraveNewWorldWrapper(pdi: PlatformDependentInfo) : ApplicationWrapper({ BraveNewWorldInner(pdi) })

class BraveNewWorldInner(pdi: PlatformDependentInfo) : ApplicationInner(pdi) {

    init {
    }
    val batch = SpriteBatch()
    val img = Texture("badlogic.jpg")

    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
   }
}
