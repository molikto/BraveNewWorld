package org.snailya.bnw.gamelogic.def



// TODO not used. elemental is things that make the material world??
sealed class Elemental(
) : Def


object H2O : Elemental()

class Mineral(val tintColor: Int) : Elemental() {
}


class Wood(val tintColor: Int) : Elemental() {
}


val SandstoneMineral = Mineral(0x7c6d5aFF)

val MarbleMineral = Mineral(0xc380f0FF.toInt())

val TempWood = Wood(0xFFFFFFFF.toInt())
