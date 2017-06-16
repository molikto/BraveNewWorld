package org.snailya.bnw

import com.badlogic.gdx.math.Vector2
import org.snailya.base.KotlinSerializationAdapter
import java.nio.ByteBuffer

/**
 * Created by molikto on 15/06/2017.
 */

object Vector2Adapter : KotlinSerializationAdapter<Vector2>() {
    override fun parse(b: ByteBuffer): Vector2 {
        return Vector2(b.getFloat(), b.getFloat())
    }

    override fun serialize(b: ByteBuffer, t: Vector2) {
        b.putFloat(t.x)
        b.putFloat(t.y)
    }

}
