package org.snailya.bnw.gamelogic.def


/**
 * is anything that blocks the passage
 * it can be mine-able blocks, human made buildings
 */
open class BlockageType(
        val texture: TextureRef,
        val connectRoof: Boolean,
        val baseWalkSpeed: Float = 0F) : Def {
    init {
        // TODO we don't allow any free form blockage that is roof high but allow walk through??
        assert(!connectRoof || baseWalkSpeed == 0F)
    }
}
