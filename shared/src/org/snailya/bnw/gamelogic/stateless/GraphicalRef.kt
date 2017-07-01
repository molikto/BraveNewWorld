package org.snailya.bnw.gamelogic.stateless


/**
 * TextureRef
 *
 * texture element needed for rendering
 * render method is defined totally in rendering code, not here
 *
 * the name is a relative defined thing mostly...
 */

sealed class TextureRef(open val name: String)
data class SimpleTextureRef(override val name: String) : TextureRef(name)
data class TintedTextureRef(override val name: String, val color: Int /* rgba8888 */) : TextureRef(name)
