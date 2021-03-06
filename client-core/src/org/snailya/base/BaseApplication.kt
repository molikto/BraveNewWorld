package org.snailya.base

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import org.snailya.base.logging.info

private var _app: ApplicationInner? = null
// reference Gdx.app by Gdx.app ...
val app by lazy { _app!! }

/**
 * to calculate REAL logical size, because LibGDX's method is wrong
 * the reason we do this is because we want to be consistent with https://material.io/devices/
 */
class PlatformDependentInfo(val iOSScale: Float?, val logicalWidth: Int?)



abstract class ApplicationInner(pdi: PlatformDependentInfo) {

    init {
        _app = this
    }

    fun backBufferWidth() = graphics.backBufferWidth
    fun backBufferHeight() = graphics.backBufferHeight

    val shaderDefault = shaderOf("default")

    val batch = SpriteBatch(100, shaderDefault)

    /**
     * these are calculated ourselves, seems good to NOT use LIBGDX's API
     */
    val dpiPixel: Float = pdi.iOSScale ?: (if (pdi.logicalWidth != null) (backBufferWidth().toFloat() / pdi.logicalWidth) else graphics.density)

    fun width(): Float = backBufferWidth() / dpiPixel
    fun height(): Float = backBufferHeight() / dpiPixel

    // TODO logical size image loader

    init {
        info {
            "Pixel density: $dpiPixel," +
                    " w0: ${graphics.width}, h0: ${graphics.height}," +
                    " w: ${width()}, h: ${height()}," +
                    " rw: ${backBufferWidth()}, rh: ${backBufferHeight()}"
        }
    }

    lateinit var page: Page

    fun change(p: () -> Page) {
        page.dispose()
        page = p()
    }

    fun postCreate() {
        input.inputProcessor = object : InputProcessor {
            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean =
                    page.uiStage.touchUp(screenX, screenY, pointer, button) ||
                            page.inputProcessor?.touchUp(screenX, screenY, pointer, button) ?: false

            override fun mouseMoved(screenX: Int, screenY: Int): Boolean =
                    page.uiStage.mouseMoved(screenX, screenY) ||
                            page.inputProcessor?.mouseMoved(screenX, screenY) ?: false

            override fun keyTyped(character: Char): Boolean =
                    page.uiStage.keyTyped(character) ||
                            page.inputProcessor?.keyTyped(character) ?: false

            override fun scrolled(amount: Int): Boolean =
                    page.uiStage.scrolled(amount) ||
                            page.inputProcessor?.scrolled(amount) ?: false

            override fun keyUp(keycode: Int): Boolean =
                    page.uiStage.keyUp(keycode) ||
                            page.inputProcessor?.keyUp(keycode) ?: false

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean =
                    page.uiStage.touchDragged(screenX, screenY, pointer) ||
                            page.inputProcessor?.touchDragged(screenX, screenY, pointer) ?: false

            override fun keyDown(keycode: Int): Boolean =
                    page.uiStage.keyDown(keycode) ||
                            page.inputProcessor?.keyDown(keycode) ?: false

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean =
                    page.uiStage.touchDown(screenX, screenY, pointer, button) ||
                            page.inputProcessor?.touchDown(screenX, screenY, pointer, button) ?: false

        }
    }

    // maybe good place to stop/start game logic??
    open fun resume() = page.resume()

    open fun pause() = page.pause()
    open fun dispose() {
        page.disposeInner()
        batch.dispose()
    }

    var renderTime = 0L

    open fun render() {
        val a = System.currentTimeMillis()
        page.renderInner()
        renderTime = (System.currentTimeMillis() - a)
    }

    fun resize(width: Int, height: Int) = page.resizeInner(width, height)
}

open class ApplicationWrapper(val factory: () -> ApplicationInner) : ApplicationAdapter() {

    lateinit var inner: ApplicationInner
    override fun create() {
        inner = factory()
        inner.postCreate()
    }

    override fun pause() = inner.pause()

    override fun resize(width: Int, height: Int) = inner.resize(width, height)

    override fun render() = inner.render()

    override fun resume() = inner.resume()

    override fun dispose() = inner.dispose()
}

