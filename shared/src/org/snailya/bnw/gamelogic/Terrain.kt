package org.snailya.bnw.gamelogic


open class Terrain(
        val texture: TextureRef,
        val grainSize: Int
) {
        val baseWalkSpeed = 1F
}

val Sand = Terrain(SimpleTextureRef("Terrain/Sand"), 1)
val Soil = Terrain(SimpleTextureRef("Terrain/Soil"), 2)
val Gravel = Terrain(SimpleTextureRef("Terrain/Gravel"), 3)

class HewnRock(
        val rock: Mineral
) : Terrain(TintedTextureRef("Terrain/HewnRock", rock.tintColor), 10)

val SandstoneHewnRock = HewnRock(SandstoneMineral)


// TODO what to do with spreadsheet data??
val NaturalTerrains = listOf(Sand, Soil, Gravel, SandstoneHewnRock)
val NaturalTerrainsByGrainSize = NaturalTerrains.sortedBy { it.grainSize }
val NaturalTerrainsByGrainSizeInverse = NaturalTerrains.sortedBy { -it.grainSize }

class StoneTerrain(
        val mineral: Mineral
) : Terrain(TintedTextureRef("Terrain/Stone", mineral.tintColor), 10)


