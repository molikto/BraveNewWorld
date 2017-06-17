package org.snailya.base


inline val Int.tf: Float
    inline get() = this.toFloat()


inline fun <T> configured(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}

fun tif(s: String) = println("${System.currentTimeMillis()}:  $s")


