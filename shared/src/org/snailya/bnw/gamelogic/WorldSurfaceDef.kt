package org.snailya.bnw.gamelogic


abstract class WorldSurface(
        val baseWalkSpeed: Float
) {
    val walkable = baseWalkSpeed > 0
}

class Water(
        val texture: TextureRef,
        val depth: Int // a
) : WorldSurface(if (depth == 0) 1F else 0F)

val DeepWater = Water(SimpleTextureRef("DeepWater"), 1)
val ShallowWater = Water(SimpleTextureRef("ShallowWater"), 0)

val Waters = listOf(DeepWater, ShallowWater)
val WatersByDepth = Waters.sortedBy { it.depth }

open class NaturalTerrain(
        val texture: TextureRef,
        val grainSize: Int
): WorldSurface(1F)


val Sand = NaturalTerrain(SimpleTextureRef("Sand"), 1)

val Soil = NaturalTerrain(SimpleTextureRef("Soil"), 2)

val Gravel = NaturalTerrain(SimpleTextureRef("Gravel"), 3)

class HewnRock(
        val rockType: MineralType
) : NaturalTerrain(TintedTextureRef("HewnRock", rockType.tintColor), 10)

val SandstoneHewnRock = HewnRock(MineralType.Sandstone)


// TODO what to do with spreadsheet data??
val NaturalTerrains = listOf(Sand, Soil, Gravel, SandstoneHewnRock)
val NaturalTerrainsByGrainSize = NaturalTerrains.sortedBy { it.grainSize }
val NaturalTerrainsByGrainSizeInverse = NaturalTerrains.sortedBy { -it.grainSize }

class Stone(
        val rockType: MineralType
) : NaturalTerrain(TintedTextureRef("Stone", rockType.tintColor), 10)


/**
 * ConstructedFloor
 */
data class ConstructedFloor(
        val texture: String
)
