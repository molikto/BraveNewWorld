package org.snailya.base



class StrictVector2(var x: Float, var y: Float) {
    companion object {
        val Zero = StrictVector2(0F, 0F)
    }

    @Strictfp
    fun copy(): StrictVector2 {
        return StrictVector2(x, y)
    }

    @Strictfp
    fun len(): Float {
        return StrictMath.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    @Strictfp
    fun nor(): StrictVector2 {
        val len = len()
        if (len != 0f) {
            x /= len
            y /= len
        }
        return this
    }

    @Strictfp
    inline fun ivec2() = IntVector2(x.toInt(), y.toInt())
    @Strictfp
    inline fun  set(position: StrictVector2) {
        this.x = position.x
        this.y = position.y
    }
    @Strictfp
    inline fun  set(position: IntVector2) {
        this.x = position.x.toFloat()
        this.y = position.y.toFloat()
    }
}

@Strictfp
operator fun  StrictVector2.plus(position: StrictVector2): StrictVector2 {
    this.x += position.x
    this.y += position.y
    return this
}

@Strictfp
operator fun StrictVector2.minus(position: StrictVector2): StrictVector2 {
    this.x -= position.x
    this.y -= position.y
    return this
}

@Strictfp
operator fun  StrictVector2.times(p: Int): StrictVector2 {
    this.x *= p
    this.y *= p
    return this
}

@Strictfp
operator fun  StrictVector2.div(p: Int): StrictVector2 {
    this.x /= p
    this.y /= p
    return this
}


@Strictfp
operator fun  StrictVector2.times(p: Float): StrictVector2 {
    this.x *= p
    this.y *= p
    return this
}

@Strictfp
operator fun  StrictVector2.div(p: Float): StrictVector2 {
    this.x /= p
    this.y /= p
    return this
}
