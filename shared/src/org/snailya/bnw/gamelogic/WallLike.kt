package org.snailya.bnw.gamelogic

import org.snailya.bnw.gamelogic.def.WallLikeType


class WallLike(val type: WallLikeType) : Planted {
    override val walkM: Float = type.walkM
    override val sightM: Float = type.sightM
    override val coverage: Float = type.coverage
    var health: Int = 100
}


