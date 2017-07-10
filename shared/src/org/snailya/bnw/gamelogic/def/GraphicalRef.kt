package org.snailya.bnw.gamelogic.def



/**
 * TextureRef
 *
 * texture element needed for rendering
 * render method is defined totally in rendering code, not here
 *
 * the name is a relative defined thing mostly...
 */

data class TextureRef(val name: String, val color: Float = ColorWhite) : Def {
}
