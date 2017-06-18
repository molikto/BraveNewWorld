package org.snailya.base

/**
 * Created by molikto on 18/06/2017.
 */

class IntVector2(var x: Int, var y: Int) {

    companion object {
        val Zero = ivec2()
    }
    fun svec() = StrictVector2(x.toFloat(), y.toFloat())

    fun  set(position: IntVector2) {
        this.x = position.x
        this.y = position.y
    }
    fun  set(position: StrictVector2) {
        this.x = position.x.toInt()
        this.y = position.y.toInt()
    }
}
