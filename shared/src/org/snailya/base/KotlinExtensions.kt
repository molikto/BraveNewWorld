package org.snailya.base


inline val Int.tf: Float
    inline get() = this.toFloat()


inline fun <T> configured(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}
var logger: (String) -> Unit = {}

fun tif(s: String) = logger("${System.currentTimeMillis()}:  $s")


inline fun <T> time(str: String, b: () -> T): T {
    val t = System.currentTimeMillis()
    val a = b.invoke()
    tif("timed ${System.currentTimeMillis() - t}: $str")
    return a
}

inline fun timet(str: String, b: () -> String) {
    val t = System.currentTimeMillis()
    var res = b.invoke()
    if (!res.isEmpty()) res = ", " + res
    tif("timed ${System.currentTimeMillis() - t}: $str$res")
}


inline fun svec2(x: Float = 0F, y: Float = 0F) = StrictVector2(x, y)

inline fun ivec2(x: Int = 0, y: Int = 0) = IntVector2(x, y)



