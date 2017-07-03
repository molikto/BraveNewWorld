package org.snailya.bnw.gamelogic.def


/**
 * TextureRef
 *
 * texture element needed for rendering
 * render method is defined totally in rendering code, not here
 *
 * the name is a relative defined thing mostly...
 */

// TODO tint color is off
data class TextureRef(val name: String, val color: Int /* rgba8888 */ = 0xFFFFFFFF.toInt()) : Def {
    val colorFloatBits  = java.lang.Float.intBitsToFloat(color and (0xfeffffff.toInt()))
}
