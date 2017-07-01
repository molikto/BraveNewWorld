package org.snailya.bnw.gamelogic

import org.snailya.bnw.ps


open class NaturalTerrain(
        val texture: TextureRef,
        val grainSize: Int
) {
        val baseWalkSpeed = 1F
}

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


