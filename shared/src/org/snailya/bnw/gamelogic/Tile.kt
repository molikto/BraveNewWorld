package org.snailya.bnw.gamelogic

import org.serenaz.InputPoint
import org.snailya.base.IntVector2
import org.snailya.base.StrictVector2
import org.snailya.bnw.gamelogic.stateless.ConstructedFloor
import org.snailya.bnw.gamelogic.stateless.DeepWater
import org.snailya.bnw.gamelogic.stateless.Terrain
import org.snailya.bnw.gamelogic.stateless.WaterSurface


// stateful

class Tile(
        val position: IntVector2
) {
    lateinit var terrain: Terrain
    var waterSurface: WaterSurface? = null
    var floor: ConstructedFloor? = null

    // TODO these are temp
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


    // temp values used by route finder
    var temp_cost: Float = 0F
    var temp_priority: Float = 0F
    var temp_visited: Int = -1
    var temp_ttpo: IntVector2 = IntVector2.Zero // to the previous of

    // links to map generator
    lateinit var debug_inputPoint: InputPoint
}
