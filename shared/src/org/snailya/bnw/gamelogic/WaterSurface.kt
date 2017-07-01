package org.snailya.bnw.gamelogic

/**
 * Created by molikto on 01/07/2017.
 */


class WaterSurface(
        val texture: TextureRef,
        val depth: Int // a
)

val DeepWater = WaterSurface(SimpleTextureRef("DeepWater"), 1)
val ShallowWater = WaterSurface(SimpleTextureRef("ShallowWater"), 0)

val Waters = listOf(DeepWater, ShallowWater)
val WatersByDepth = Waters.sortedBy { it.depth }
