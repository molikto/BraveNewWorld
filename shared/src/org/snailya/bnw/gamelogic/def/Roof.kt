package org.snailya.bnw.gamelogic.def


class Roof(val name: String, val height: Int, val constructable: Boolean) : Def

val ConstructedRoof = Roof("Constructed Roof", 1, true)

val ThinRockRoof = Roof("Thin Rock Roof", 1, false)

val OverheadMountain = Roof("Overhead Mountain", 10, false)




