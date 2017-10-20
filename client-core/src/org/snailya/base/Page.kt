package org.snailya.base

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.scene2d.KTableWidget
import ktx.scene2d.KWidget
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.graphics.GL20

/**
 * Created by molikto on 07/08/2017.
 */

fun simplePage(uip: () -> KTableWidget?): Page = object : Page() {
    init {
        ui = uip()
    }
}

abstract class Page {

    //fun uiViewport() = ScalingViewport(Scaling.stretch, game.backBufferWidth().toFloat(), game.backBufferHeight().toFloat(), OrthographicCamera())
    fun uiViewport() = ScreenViewport(OrthographicCamera())

    val uiStage = Stage(uiViewport(), app.batch)
    var inputProcessor: InputProcessor? = null
    var clearColor = Color.BLACK
    val batch = app.batch

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
