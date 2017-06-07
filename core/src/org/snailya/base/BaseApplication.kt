package org.snailya.base

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch


/**
 * to calculate REAL logical size, because LibGDX's method is wrong
 * the reason we do this is because we want to be consistent with https://material.io/devices/
 */
class PlatformDependentInfo(val iOSScale: Float?, val logicalWidth: Int?)

abstract class ApplicationInner(pdi: PlatformDependentInfo) {

    val dpiPixel: Float = pdi.iOSScale ?: (if (pdi.logicalWidth != null) (backBufferWidth().toFloat() / pdi.logicalWidth) else Gdx.graphics.density)

    fun backBufferWidth() = Gdx.graphics.backBufferWidth
    fun backBufferHeight() = Gdx.graphics.backBufferHeight

    fun width(): Float = backBufferWidth() / dpiPixel
    fun height(): Float = backBufferHeight() / dpiPixel


    init {
        debug("Pixel density: $dpiPixel," +
                " w0: ${Gdx.graphics.width}, h: ${Gdx.graphics.height}," +
                " w: ${width()}, h: ${height()}," +
                " rw: ${backBufferWidth()}, rh: ${backBufferHeight()}")
    }



    fun resume() {}
    fun pause() {}
    open fun render() {}
    open fun dispose() {}
    fun resize(width: Int, height: Int) {}
}

open class ApplicationWrapper(val factory: () -> ApplicationInner) : ApplicationAdapter() {

    lateinit var inner: ApplicationInner
    override fun create() {
        inner = factory()
    }

    override fun pause() = inner.pause()

    override fun resize(width: Int, height: Int) = inner.resize(width, height)

    override fun render() = inner.render()

    override fun resume() = inner.resume()

    override fun dispose() = inner.dispose()
}

