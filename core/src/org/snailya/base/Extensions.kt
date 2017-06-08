package org.snailya.base

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.scene2d.KTableWidget
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.defaultStyle

/**
 * Created by molikto on 07/06/2017.
 */


/**
 * See the code how path is constructed
 */
fun fontGenerator(name: String) = FreeTypeFontGenerator(Gdx.files.internal("fonts/$name.ttf"))

fun FreeTypeFontGenerator.ofSize(i: Float): BitmapFont {
    val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
    parameter.size = i.toInt()
    return this.generateFont(parameter)
}

fun Actor.onClick(action: (x: Float, y: Float) -> Unit) = this.addListener(object : ClickListener() {
    override fun clicked(event: InputEvent, x: Float, y: Float) = action.invoke(x, y)
})

fun Actor.onClick(action: (event: InputEvent, x: Float, y: Float) -> Unit) = this.addListener(object : ClickListener() {
    override fun clicked(event: InputEvent, x: Float, y: Float) = action.invoke(event, x, y)
})


