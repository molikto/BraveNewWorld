package org.snailya.bnw.gamelogic


sealed class WorldSurface(
        val baseWalkSpeed: Float
)
{
    val walkable = baseWalkSpeed > 0
}

class WaterSurface(
        val texture: TextureRef,
        val depth: Int // a
) : WorldSurface(
        baseWalkSpeed = if (depth == 0) 1F else 0F
)

val DeepWater = WaterSurface(SimpleTextureRef("DeepWater"), 1)
val ShallowWater = WaterSurface(SimpleTextureRef("ShallowWater"), 0)

val Waters = listOf(DeepWater, ShallowWater)
val WatersByDepth = Waters.sortedBy { it.depth }

open class NaturalTerrain(
        val texture: TextureRef,
        val grainSize: Int
): WorldSurface(
        baseWalkSpeed = 1F
)

val Sand = NaturalTerrain(SimpleTextureRef("Sand"), 1)
val Soil = NaturalTerrain(SimpleTextureRef("Soil"), 2)
val Gravel = NaturalTerrain(SimpleTextureRef("Gravel"), 3)

class HewnRock(
        val rock: Mineral
) : NaturalTerrain(TintedTextureRef("HewnRock", rock.tintColor), 10)

val SandstoneHewnRock = HewnRock(SandstoneMineral)


// TODO what to do with spreadsheet data??
val NaturalTerrains = listOf(Sand, Soil, Gravel, SandstoneHewnRock)
val NaturalTerrainsByGrainSize = NaturalTerrains.sortedBy { it.grainSize }
val NaturalTerrainsByGrainSizeInverse = NaturalTerrains.sortedBy { -it.grainSize }

class StoneTerrain(
        val mineral: Mineral
) : NaturalTerrain(TintedTextureRef("Stone", mineral.tintColor), 10)


/**
 * ConstructedFloor
 */
data class ConstructedFloor(
        val texture: String
)
