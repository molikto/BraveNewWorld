package org.snailya.base

import java.nio.ByteBuffer

/**
 * Created by molikto on 18/06/2017.
 */



object IntVector2Adapter : KotlinSerializationAdapter<IntVector2>() {
    override fun parse(b: ByteBuffer): IntVector2 {
        return IntVector2(b.getInt(), b.getInt())
    }

    override fun serialize(b: ByteBuffer, t: IntVector2) {
        b.putInt(t.x)
        b.putInt(t.y)
    }
}

object StrictVector2Adapter : KotlinSerializationAdapter<StrictVector2>() {
    override fun parse(b: ByteBuffer): StrictVector2 {
        return StrictVector2(b.getFloat(), b.getFloat())
    }

    override fun serialize(b: ByteBuffer, t: StrictVector2) {
        b.putFloat(t.x)
        b.putFloat(t.y)
    }

}
