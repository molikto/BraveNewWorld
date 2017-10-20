package org.snailya.base.simpleserialization

import org.snailya.base.math.IntVector2
import org.snailya.base.strictmath.StrictVector2
import java.nio.ByteBuffer

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
