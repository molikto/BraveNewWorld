package org.snailya.bnw.gamelogic

import org.snailya.base.svec2
import java.util.*


class MapGen(random: Random, size: Int) {

    init {
        val randomDots = (0 .. size * 2).map {
            svec2(1 + random.nextFloat() * (size - 2), 1 + random.nextFloat() * (size - 2))
        }
    }
}
