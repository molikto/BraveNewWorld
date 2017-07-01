package org.snailya.bnw.gamelogic.stateless

class WaterSurface(
        val texture: TextureRef,
        val depth: Int // a
) {
    // it is just a mark, can be used by other things,
    // for
    val isDeep get() = depth >= 1

    val isShallow get() = depth < 1
}

val DeepWater = WaterSurface(SimpleTextureRef("WaterSurface/DeepWater"), 1)
val ShallowWater = WaterSurface(SimpleTextureRef("WaterSurface/ShallowWater"), 0)

val Waters = listOf(DeepWater, ShallowWater)
val WatersByDepth = Waters.sortedBy { it.depth }
