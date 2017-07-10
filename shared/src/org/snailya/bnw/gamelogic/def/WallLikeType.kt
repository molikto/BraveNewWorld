package org.snailya.bnw.gamelogic.def

import org.snailya.bnw.gamelogic.Planted

class TextureAtlasComponent()


class WallLikeType(val textureAtlas: TextureRef) : Planted {
    override val walkM: Float = 0F
    override val sightM: Float = 0F
    override val coverage: Float = 0.5F // TODO coverage for wall?
}

val SandstoneMineralWall = Sandstone.mineralWallType()

val WallLikeTypes = listOf<WallLikeType>(
        SandstoneMineralWall
)


