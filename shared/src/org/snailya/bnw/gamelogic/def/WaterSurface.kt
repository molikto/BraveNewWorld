package org.snailya.bnw.gamelogic.def

import org.snailya.bnw.gamelogic.Planted

data class WaterSurface(
        val texture: TextureRef,
        val depth: Float // 0 - 1 depth, 1 means super deep
): Def, Planted {
    override val walkM: Float = (1 - depth)
    override val sightM: Float = 1F
    override val coverage: Float = 0F
}

fun isDeepWaterSurface(p: Planted?): Boolean {
    if (p is WaterSurface) {
        return p.depth == 1F
    } else {
        return false
    }
}

val DeepWater = WaterSurface(TextureRef("WaterSurface/DeepWater"), 1F)
val ShallowWater = WaterSurface(TextureRef("WaterSurface/ShallowWater"), 0.1F)

val Waters = listOf(
        DeepWater,
        ShallowWater
)

val WatersByDepth = Waters.sortedBy { it.depth }
