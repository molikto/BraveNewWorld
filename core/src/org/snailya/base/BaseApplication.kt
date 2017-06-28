package org.snailya.base

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.scene2d.KTableWidget
import ktx.log.*
import ktx.scene2d.KStack
import ktx.scene2d.KTable
import ktx.scene2d.KWidget

private var _game: ApplicationInner? = null
val game by lazy { _game!! }

inline val Int.dp: Float
    inline get() = game.dpiPixel * this

inline val Float.dp: Float
    inline get() = game.dpiPixel * this


/**
 * to calculate REAL logical size, because LibGDX's method is wrong
 * the reason we do this is because we want to be consistent with https://material.io/devices/
 */
class PlatformDependentInfo(val iOSScale: Float?, val logicalWidth: Int?)

fun simplePage(uip: () -> KTableWidget?): Page = object : Page() {
    init {
        ui = uip()
    }
}

abstract class Page {

    //fun uiViewport() = ScalingViewport(Scaling.stretch, game.backBufferWidth().toFloat(), game.backBufferHeight().toFloat(), OrthographicCamera())
    fun uiViewport() = ScreenViewport(OrthographicCamera())

    val uiStage = Stage(uiViewport(), game.batch)
    var inputProcessor: InputProcessor? = null
    var clearColor = Color.BLACK
    val batch = game.batch

    var ui: KWidget<*>? = null
        set(value) {
            if (value != null) {
                if (value is WidgetGroup) value.setFillParent(true)
                uiStage.addActor(value as Actor)
            } else {
                uiStage.clear()
            }
        }

    open fun resume() {}
    open fun pause() {}

    fun disposeInner() {
        dispose()
        uiStage.dispose()
    }

    open fun dispose() {}

    fun renderInner() {
        gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = identityMatrix4
        uiStage.act() // TODO why 30fps??
        render()
        uiStage.draw()
    }

    open fun render() {
    }

    fun resizeInner(width: Int, height: Int) {
        uiStage.viewport.update(width, height, true)
        resize(width, height)
    }

    open fun resize(width: Int, height: Int) {
    }
}


abstract class ApplicationInner(pdi: PlatformDependentInfo) {

    init {
        _game = this
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

