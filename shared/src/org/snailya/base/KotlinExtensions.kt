package org.snailya.base


inline val Int.tf: Float
    inline get() = this.toFloat()


inline fun <T> configured(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}

fun tif(s: String) = println("${System.currentTimeMillis()}:  $s")


inline fun time(str: String, b: () -> Unit) {
    val t = System.currentTimeMillis()
    b.invoke()
    tif("timed ${System.currentTimeMillis() - t}: $str")
}

inline fun timet(str: String, b: () -> String) {
    val t = System.currentTimeMillis()
    var res = b.invoke()
    if (!res.isEmpty()) res = ", " + res
    tif("timed ${System.currentTimeMillis() - t}: $str$res")
}


