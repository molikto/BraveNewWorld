package org.snailya.bnw.gamelogic

import java.io.Serializable


class Order : Serializable {
}

class MasterMind : Serializable {
    val orders = mutableListOf<Order>()
}


