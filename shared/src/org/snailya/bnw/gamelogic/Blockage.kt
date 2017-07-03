package org.snailya.bnw.gamelogic

import org.snailya.base.IntVector2
import org.snailya.bnw.gamelogic.def.FreeFormBlockageType
import java.io.Serializable


open class Blockage : Serializable {
}


class FreeFormBlockage(val type: FreeFormBlockageType) : Blockage() {
    var health = 255
}

class PartOfBuildingBlockage(val building: Building, val part: IntVector2) : Blockage() {
}




