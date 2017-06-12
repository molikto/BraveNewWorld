package org.snailya.base

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.math.vec2
import ktx.math.vec3
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


/**
 * Kotlin math functions
 */

fun Matrix4.setToOrtho2DCentered(cx: Float, cy: Float, w: Float, h: Float): Matrix4 =
        this.setToOrtho2D(cx - w / 2, cy - h / 2, w, h)

fun Vector2.copy(x: Float = this.x, y: Float = this.y) = vec2(x, y)

fun Vector3.copy(x: Float = this.x, y: Float = this.y, z: Float = this.z) = vec3(x, y, z)

fun Matrix4.copy() = this.cpy()!!


fun Vector2.extends(z: Float = 0F): Vector3 = vec3(this.x, this.y, z)

fun Vector3.lose() = vec2(this.x, this.y)

val UnitVector = vec2(1F, 1F)

val identityMatrix4 = Matrix4()

fun identityMatrix4() = Matrix4()

/**
 * Kotlin generic extensions
 *
 */

inline val Int.tf: Float
    inline get() = this.toFloat()


inline fun <T> configured(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}

