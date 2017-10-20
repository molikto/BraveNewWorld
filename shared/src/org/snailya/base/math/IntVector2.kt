package org.snailya.base.math

import org.snailya.base.strictmath.StrictVector2
import java.io.Serializable

inline fun ivec2(x: Int = 0, y: Int = 0) = IntVector2(x, y)

data class IntVector2(@JvmField var x: Int, @JvmField var y: Int) : Serializable {

    companion object {
        val Zero = ivec2()
    }

    fun len(): Float = StrictMath.sqrt((x * x + y * y).toDouble()).toFloat()

    fun  set(position: IntVector2) {
        this.x = position.x
        this.y = position.y
    }
    fun  set(position: StrictVector2) {
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


