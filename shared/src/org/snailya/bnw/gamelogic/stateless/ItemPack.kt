package org.snailya.bnw.gamelogic.stateless

import org.snailya.bnw.gamelogic.def.Item
import java.io.Serializable

class ItemPack(
        val item: Item,
        val number: Int
) : Serializable {
    init {
        assert(number > 0)
    }
}
