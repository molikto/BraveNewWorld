package org.snailya.bnw.gamelogic.def


/**
 *
 * these representation should be opaque to other things in [org.snailya.bnw.gamelogic.def] package
 *
 */

fun colorOf(r: Int, g: Int, b: Int, a: Int = 0xFF): Float {
    val color = (a shl 24) or (b shl 16) or (g shl 8) or r
    return java.lang.Float.intBitsToFloat(color and -16777217)
}

val ColorWhite = colorOf(0xFF, 0xFF, 0xFF)
val ColorTest = colorOf(0x00, 0x2b, 0x36)
