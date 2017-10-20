package org.snailya.base.lang

inline fun <T> configured(receiver: T, block: T.() -> Unit): T {
    receiver.block()
    return receiver
}
