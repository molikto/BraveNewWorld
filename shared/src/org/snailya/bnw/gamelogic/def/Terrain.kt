package org.snailya.bnw.gamelogic.def


open class Terrain(
        val texture: TextureRef,
        val grainSize: Int
): Def {
        val baseWalkSpeed = 1F
}

val Sand = Terrain(TextureRef("Terrain/Sand"), 1)
val Soil = Terrain(TextureRef("Terrain/Soil"), 2)
val Gravel = Terrain(TextureRef("Terrain/Gravel"), 3)

class HewnRock(
        val rock: Mineral
) : Terrain(TextureRef("Terrain/HewnRock", rock.tintColor), 10)

val SandstoneHewnRock = HewnRock(SandstoneMineral)


// TODO what to do with spreadsheet data??
val NaturalTerrains = listOf(Sand, Soil, Gravel, SandstoneHewnRock)
val NaturalTerrainsByGrainSize = NaturalTerrains.sortedBy { it.grainSize }
val NaturalTerrainsByGrainSizeInverse = NaturalTerrains.sortedBy { -it.grainSize }

class StoneTerrain(
        val mineral: Mineral
) : Terrain(TextureRef("Terrain/Stone", mineral.tintColor), 10)


