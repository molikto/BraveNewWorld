package org.snailya.base

/**
 * Created by molikto on 18/06/2017.
 */

data class IntVector2(@JvmField var x: Int, @JvmField var y: Int) {

    companion object {
        val Zero = ivec2()
    }
    fun svec() = SVector2(x.toFloat(), y.toFloat())

    fun len(): Float = StrictMath.sqrt((x * x + y * y).toDouble()).toFloat()

    fun  set(position: IntVector2) {
        this.x = position.x
        this.y = position.y
    }
    fun  set(position: SVector2) {
        this.x = position.x.toInt()
        this.y = position.y.toInt()
    }
}



operator fun  IntVector2.plus(position: IntVector2): IntVector2 {
    this.x += position.x
    this.y += position.y
    return this
}

operator fun IntVector2.minus(position: IntVector2): IntVector2 {
    this.x -= position.x
    this.y -= position.y
    return this
}

operator fun  IntVector2.times(p: Int): IntVector2 {
    this.x *= p
    this.y *= p
    return this
}

operator fun  IntVector2.div(p: Int): IntVector2 {
    this.x /= p
    this.y /= p
    return this
}


