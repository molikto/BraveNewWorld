package org.snailya.bnw.gamelogic



sealed class Element(
)


object H2O : Element()

class Mineral(val tintColor: Int) : Element() {
}



val SandstoneMineral = Mineral(0xa020f0ff.toInt())

val MarbleMineral = Mineral(0xc380f0ff.toInt())
