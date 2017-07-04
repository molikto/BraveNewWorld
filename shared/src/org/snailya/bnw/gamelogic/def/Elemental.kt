package org.snailya.bnw.gamelogic.def



// TODO not used. elemental is things that make the material world??
sealed class Elemental(
) : Def


object H2O : Elemental()

class Mineral(val tintColor: Float) : Elemental() {
    fun hewnRock() = Terrain(TextureRef("Terrain/HewnRock", tintColor), 0.9F)
    fun stoneTerrain() = Terrain(TextureRef("Terrain/Stone", tintColor), 0.9F)
    fun mineralWallType() = WallLikeType(TextureRef("WallLikeType/Mineral", tintColor))
}


class Wood(val tintColor: Float) : Elemental() {
}


val Sandstone = Mineral(colorOf(0x7c,0x6d, 0x5a))

