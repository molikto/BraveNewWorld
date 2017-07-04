package org.snailya.bnw.gamelogic.def


open class Terrain(
        val texture: TextureRef,
        val grainSize: Float
): Def {
    companion object {
        val minimalSpeedM = 0.7F
    }
    val walkM = minimalSpeedM + grainSize * (1 - minimalSpeedM)
}

val Sand = Terrain(TextureRef("Terrain/Sand"), 0.1F)
val Soil = Terrain(TextureRef("Terrain/Soil"), 0.2F)
val Gravel = Terrain(TextureRef("Terrain/Gravel"), 0.4F)

val SandstoneHewnRock = Sandstone.hewnRock()

// TODO what to do with spreadsheet data??
val NaturalTerrains = listOf(
        Sand,
        Soil,
        Gravel,
        SandstoneHewnRock
)


val NaturalTerrainsByGrainSize = NaturalTerrains.sortedBy { it.grainSize }
val NaturalTerrainsByGrainSizeInverse = NaturalTerrains.sortedBy { -it.grainSize }
