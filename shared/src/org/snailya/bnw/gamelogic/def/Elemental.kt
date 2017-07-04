package org.snailya.bnw.gamelogic.def



// TODO not used. elemental is things that make the material world??
sealed class Elemental(
) : Def


object H2O : Elemental()

class Mineral(val tintColor: Int) : Elemental() {
    fun hewnRock() = Terrain(TextureRef("Terrain/HewnRock", tintColor), 0.9F)
    fun stoneTerrain() = Terrain(TextureRef("Terrain/Stone", tintColor), 0.9F)
    fun mineralWallType() = WallLikeType(TextureRef("WallLikeType/Mineral", tintColor))
}


class Wood(val tintColor: Int) : Elemental() {
}


val Sandstone = Mineral(0x7c6d5aFF)

val MarbleMineral = Mineral(0xc380f0FF.toInt())

val TempWood = Wood(0xFFFFFFFF.toInt())
