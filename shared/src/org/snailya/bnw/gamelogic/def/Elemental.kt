package org.snailya.bnw.gamelogic.def



// TODO not used. elemental is things that make the material world??
sealed class Elemental(
) : Def


object H2O : Elemental()

class Mineral(val tintColor: Int) : Elemental() {
}



val SandstoneMineral = Mineral(0xa020f0ff.toInt())

val MarbleMineral = Mineral(0xc380f0ff.toInt())