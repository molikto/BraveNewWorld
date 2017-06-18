package org.snailya.bnw




/**
 * basic config
 * they are now constants, but we might change them to runtime values
 */
const val timePerTick = 100
const val timePerGameTick = 20
const val gameTickPerTick = timePerTick / timePerGameTick
const val gameTickPerSecond = 1000 / timePerGameTick

object GameConfigAsserts {
    init {
        assert(timePerTick % timePerGameTick == 0)
        assert(1000 % timePerGameTick == 0)
    }
}

// second value to per tick value
val Float.ps
    get() = this / gameTickPerSecond

