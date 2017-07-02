package org.snailya.base


inline val Int.tf: Float
    inline get() = this.toFloat()


inline fun <T> configured(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}


inline fun svec2(x: Float = 0F, y: Float = 0F) = SVector2(x, y)

inline fun ivec2(x: Int = 0, y: Int = 0) = IntVector2(x, y)



