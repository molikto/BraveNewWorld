package org.snailya.bnw.gamelogic

import org.serenaz.InputPoint
import org.snailya.base.math.IntVector2
import org.snailya.base.strictmath.StrictVector2
import org.snailya.bnw.gamelogic.def.*
import org.snailya.bnw.gamelogic.stateless.ItemPack
import java.io.Serializable


class Tile(
        val position: IntVector2
) : Serializable {
    var roof: Roof? = null
    var itemPack: ItemPack? = null

    // plants, trees, walls, buildings, sandbags, water...
    var planted: Planted? = null
    var floor: ConstructedFloor? = null
    lateinit var terrain: Terrain

    // temp values used by route finder
    @Transient var temp_cost: Float = 0F
    @Transient var temp_priority: Float = 0F
    @Transient var temp_visited: Int = -1
    @Transient var temp_ttpo: IntVector2 = IntVector2.Zero // to the previous of

    // links to map generator
    @Transient lateinit var debug_inputPoint: InputPoint

    fun assertValid() {
        if (planted is WaterSurface) assert(floor == null)
    }

    init {
        assertValid()
    }

    // walking
    val walkM get() = terrain.walkM * (planted?.walkM ?: 1F)
    val noWalk get() = walkM == 0F

    // sighting
    val sightM get() = planted?.sightM ?: 1F
    val noSight get() = sightM == 0F


    // TODO this function is UGLY, as it takes a output parameter
    @Strictfp
    inline fun center(s: StrictVector2): StrictVector2 {
        s.x = position.x + 0.5F
        s.y = position.y + 0.5F
        return s
    }

}
