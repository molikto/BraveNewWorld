package org.snailya.bnw.gamelogic

import org.serenaz.InputPoint
import org.snailya.base.IntVector2
import org.snailya.base.StrictVector2

class Tile(
        val position: IntVector2
) {
    lateinit var terrain: NaturalTerrain
    var waterSurface: WaterSurface? = null
    var floor: ConstructedFloor? = null
    val wall: Unit? = null
    val walled = wall != null

    val walkable
        get() = !(walled || waterSurface == DeepWater)
    val notWalkable
        get() = !walkable


    @Strictfp
    inline fun center(s: StrictVector2): StrictVector2 {
        s.x = position.x + 0.5F
        s.y = position.y + 0.5F
        return s
    }


    var temp_cost: Float = 0F
    var temp_priority: Float = 0F
    var temp_visited: Int = -1
    var temp_ttpo: IntVector2 = IntVector2.Zero // to the previous of

    lateinit var debug_inputPoint: InputPoint
}
