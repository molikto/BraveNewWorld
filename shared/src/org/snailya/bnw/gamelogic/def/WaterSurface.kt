package org.snailya.bnw.gamelogic.def

import org.snailya.bnw.gamelogic.Planted

data class WaterSurface(
        val texture: TextureRef,
        val depth: Float // a
): Def, Planted {
    override val walkM: Float = (1 - depth)
    override val sightM: Float = 1F
    override val coverage: Float = 0F
}

val DeepWater = WaterSurface(TextureRef("WaterSurface/DeepWater"), 1F)
val ShallowWater = WaterSurface(TextureRef("WaterSurface/ShallowWater"), 0.1F)

val Waters = listOf(
        DeepWater,
        ShallowWater
)

val WatersByDepth = Waters.sortedBy { it.depth }
