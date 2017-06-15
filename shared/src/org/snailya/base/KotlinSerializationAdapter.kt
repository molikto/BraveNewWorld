@file:Suppress("UsePropertyAccessSyntax")

package org.snailya.base

import java.io.File
import java.nio.ByteBuffer


class NullTypeAdapter<T>(val base: KotlinSerializationAdapter<T>) : KotlinSerializationAdapter<T?>() {
    override fun parse(b: ByteBuffer): T? {
        if (b.get() == 0.toByte()) {
            return null
        } else {
            return base.parse(b)
        }
    }

    override fun serialize(b: ByteBuffer, t: T?) {
        if (t == null) {
            b.put(0.toByte())
        } else {
            b.put(1.toByte())
            base.serialize(b, t)
        }
    }
}

class ListTypeAdapter<T>(val base: KotlinSerializationAdapter<T>) : KotlinSerializationAdapter<List<T>>() {
    override fun parse(b: ByteBuffer): List<T> {
        val size = b.get().toInt()
        return (0 until size).map { base.parse(b) }
    }

    override fun serialize(b: ByteBuffer, t: List<T>) {
        if (t.size <= Byte.MAX_VALUE) {
            b.put(t.size.toByte())
            for (a in t) base.serialize(b, a)
        } else {
            throw Error("Size is too big, change the code!")
        }
    }
}

abstract class KotlinSerializationAdapter<T> {
    abstract fun parse(b: ByteBuffer): T
    abstract fun serialize(b: ByteBuffer, t: T)

    val nullAdapter by lazy { NullTypeAdapter(this) }

    val listAdapter by lazy { ListTypeAdapter(this) }
}

object IntAdapter : KotlinSerializationAdapter<Int>() {
    override fun parse(b: ByteBuffer): Int = b.getInt()
    override fun serialize(b: ByteBuffer, t: Int) { b.putInt(t) }
}

object LongAdapter : KotlinSerializationAdapter<Long>() {
    override fun parse(b: ByteBuffer): Long= b.getLong()
    override fun serialize(b: ByteBuffer, t: Long) { b.putLong(t) }
}

object BooleanAdapter : KotlinSerializationAdapter<Boolean>() {
    override fun parse(b: ByteBuffer): Boolean = b.get() == 1.toByte()
    override fun serialize(b: ByteBuffer, t: Boolean) { b.put((if (t) 1 else 0).toByte()) }
}




