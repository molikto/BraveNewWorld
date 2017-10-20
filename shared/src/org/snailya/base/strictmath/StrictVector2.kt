package org.snailya.base.strictmath

import org.snailya.base.math.IntVector2
import java.io.Serializable


// TODO not thread safe
/**
 * Strict Vector
 */
data class StrictVector2(@JvmField var x: Float, @JvmField var y: Float) : Serializable {
    companion object {
        val Zero = StrictVector2(0F, 0F)
    }

    @Strictfp
    inline fun copy(): StrictVector2 {
        return StrictVector2(x, y)
    }


    @Strictfp
    inline fun dis(that: StrictVector2): Float {
        val dx = (this.x - that.x)
        val dy = (this.y - that.y)
        return StrictMath.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    @Strictfp
    inline fun len2(): Float = (x * x + y * y)
    @Strictfp
    inline fun len(): Float {
        return StrictMath.sqrt(len2().toDouble()).toFloat()
    }

    @Strictfp
    inline fun nor(): StrictVector2 {
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
inline operator fun StrictVector2.plus(position: StrictVector2): StrictVector2 {
    this.x += position.x
    this.y += position.y
    return this
}

@Strictfp
inline operator fun StrictVector2.minus(position: StrictVector2): StrictVector2 {
    this.x -= position.x
    this.y -= position.y
    return this
}

@Strictfp
inline operator fun StrictVector2.times(p: Int): StrictVector2 {
    this.x *= p
    this.y *= p
    return this
}

@Strictfp
inline operator fun StrictVector2.div(p: Int): StrictVector2 {
    this.x /= p
    this.y /= p
    return this
}


@Strictfp
inline operator fun StrictVector2.times(p: Float): StrictVector2 {
    this.x *= p
    this.y *= p
    return this
}

@Strictfp
inline operator fun StrictVector2.div(p: Float): StrictVector2 {
    this.x /= p
    this.y /= p
    return this
}


@Strictfp
fun pointToLineDistance(A: StrictVector2, B: StrictVector2, P: StrictVector2): Float {
    val normalLength = StrictMath.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y).toDouble())
    return (Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength).toFloat()
}


private val temp_pointToLineDistance = StrictVector2(0F, 0F)
@Strictfp
fun pointToLineSegmentDistance(v: StrictVector2, w: StrictVector2, p: StrictVector2): Float {
    // https//stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
    // Return minimum distance between line segment vw and point p
    temp_pointToLineDistance.set(v)
    temp_pointToLineDistance - w
    val len2 = temp_pointToLineDistance.len2()
    if (len2 == 0F) return p.dis(v)
    // Consider the line extending the segment, parameterized as v + t (w - v).
    // We find projection of point p onto the line.
    // It falls where t = [(p-v) . (w-v)] / |w-v|^2
    // We clamp t from [0,1] to handle points outside the segment vw.
    var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / len2
    t = maxOf(0F, minOf(1F, t))
    temp_pointToLineDistance.set(w)
    val projection = (temp_pointToLineDistance - v) * t + v // Projection falls on the segment
    return p.dis(projection)
}