package org.snailya.base.logging


const val logLevel = 3

const val LOG_NONE = 0
const val LOG_ERROR = 1
const val LOG_INFO = 2
const val LOG_DEBUG = 3

inline fun log(tag: String, message: String) {
    println("${System.currentTimeMillis()} $tag: $message")
}

inline fun log(cause: Throwable?, tag: String, message: String) {
    println("$tag: $message")
    cause?.printStackTrace()
}

inline fun debug(tag: String = "[DEBUG]", message: () -> String) {
    if (logLevel >= LOG_DEBUG) log(tag, message())
}

inline fun debug(cause: Throwable, tag: String = "[DEBUG]", message: () -> String) {
    if (logLevel >= LOG_DEBUG) log(cause, tag, message())
}

inline fun info(tag: String = "[INFO] ", message: () -> String) {
    if (logLevel >= LOG_INFO) log(tag, message())
}

inline fun info(cause: Throwable, tag: String = "[INFO] ", message: () -> String) {
    if (logLevel >= LOG_INFO) log(cause, tag, message())
}

inline fun error(tag: String = "[ERROR]", message: () -> String) {
    if (logLevel >= LOG_ERROR) log(tag, message())
}

inline fun error(cause: Throwable, tag: String = "[ERROR]", message: () -> String) {
    if (logLevel >= LOG_ERROR) log(cause, tag, message())
}

// TODO modify them so we have `info.timed`??
inline fun <T> timed(str: String, b: () -> T): T {
    val t = System.currentTimeMillis()
    val a = b.invoke()
    info { "timed ${System.currentTimeMillis() - t}: $str" }
    return a
}

inline fun <T> nanoTimed(str: String, b: () -> T): T {
    val t = System.nanoTime()
    val a = b.invoke()
    info { "timed ${System.nanoTime() - t}: $str" }
    return a
}

inline fun timedResult(str: String, b: () -> String) {
    val t = System.currentTimeMillis()
    var res = b.invoke()
    if (!res.isEmpty()) res = ", " + res
    info { "timed ${System.currentTimeMillis() - t}: $str$res" }
}
