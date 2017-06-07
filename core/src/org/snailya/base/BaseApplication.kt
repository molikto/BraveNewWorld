package org.snailya.base

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen


/**
 * to calculate REAL logical size, because LibGDX's method is wrong
 * the reason we do this is because we want to be consistent with https://material.io/devices/
 */
class PlatformDependentInfo(val iOSScale: Float?, val logicalWidth: Int?)

abstract class Page {
    open fun resume() {}
    open fun pause() {}
    open fun dispose() {}
    open fun render() {}
    open fun resize(width: Int, height: Int) {}
}

abstract class ApplicationInner(pdi: PlatformDependentInfo) {

    fun backBufferWidth() = Gdx.graphics.backBufferWidth
    fun backBufferHeight() = Gdx.graphics.backBufferHeight

    /**
     * these are calculated ourselves, seems good to NOT use LIBGDX's API
     */
    val dpiPixel: Float = pdi.iOSScale ?: (if (pdi.logicalWidth != null) (backBufferWidth().toFloat() / pdi.logicalWidth) else Gdx.graphics.density)
    fun width(): Float = backBufferWidth() / dpiPixel
    fun height(): Float = backBufferHeight() / dpiPixel

    // TODO logical size image loader

    init {
        debug("Pixel density: $dpiPixel," +
                " w0: ${Gdx.graphics.width}, h: ${Gdx.graphics.height}," +
                " w: ${width()}, h: ${height()}," +
                " rw: ${backBufferWidth()}, rh: ${backBufferHeight()}")
    }




    lateinit var page: Page


    // maybe good place to stop/start game logic??
    open fun resume() = page.resume()
    open fun pause() = page.pause()
    open fun dispose() = page.dispose()
    open fun render() = page.render()

    fun resize(width: Int, height: Int) = page.resize(width, height)
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

