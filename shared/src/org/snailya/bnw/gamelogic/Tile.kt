package org.snailya.bnw.gamelogic

import org.serenaz.InputPoint
import org.snailya.base.IntVector2
import org.snailya.base.SVector2
import org.snailya.bnw.gamelogic.def.*
import org.snailya.bnw.gamelogic.stateless.ItemPack
import java.io.Serializable


// stateful

class Tile(
        val position: IntVector2
) : Serializable {
    var roof: Roof? = null
    var waterSurface: WaterSurface? = null
    var itemPack: ItemPack? = null

    var blockage: Blockage? = null
    var floor: ConstructedFloor? = null
    lateinit var terrain: Terrain

    fun assertValid() {
        assert(waterSurface == null || (blockage == null && floor == null))
    }

    init {
        assertValid()
    }

    val blocked get() = blockage != null
    val walkable get() = !(blocked || waterSurface?.isDeep ?: false)
    val nonWalkable get() = !walkable


    @Strictfp
    inline fun center(s: SVector2): SVector2 {
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
    @Transient lateinit var debug_inputPoint: InputPoint
}
