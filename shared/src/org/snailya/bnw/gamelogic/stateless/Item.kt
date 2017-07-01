package org.snailya.bnw.gamelogic.stateless



class ItemPack(
        val item: Item,
        val number: Int
) {
    init {
        assert(number > 0)
    }
}

class Item(
        val packSize: Int // how many of this item can be stored in one tile
)
