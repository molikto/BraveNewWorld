package org.snailya.bnw.gamelogic.def

data class WaterSurface(
        val texture: TextureRef,
        val depth: Int // a
): Def {
    // it is just a mark, can be used by other things,
    // for
    val isDeep get() = depth >= 1

    val isShallow get() = depth < 1
}

val DeepWater = WaterSurface(TextureRef("WaterSurface/DeepWater"), 1)
val ShallowWater = WaterSurface(TextureRef("WaterSurface/ShallowWater"), 0)

val Waters = listOf(DeepWater, ShallowWater)
val WatersByDepth = Waters.sortedBy { it.depth }
