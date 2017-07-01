package org.snailya.bnw.gamelogic

class WaterSurface(
        val texture: TextureRef,
        val depth: Int // a
)

val DeepWater = WaterSurface(SimpleTextureRef("WaterSurface/DeepWater"), 1)
val ShallowWater = WaterSurface(SimpleTextureRef("WaterSurface/ShallowWater"), 0)

val Waters = listOf(DeepWater, ShallowWater)
val WatersByDepth = Waters.sortedBy { it.depth }
