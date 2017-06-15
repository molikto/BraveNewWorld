
package org.snailya.bnw
import org.snailya.base.*
import java.nio.ByteBuffer

data class StartGameMessage(
        val delay: Int,
        val playerSize: Int
) {
    companion object : KotlinSerializationAdapter<StartGameMessage>() {

        override fun parse(b: ByteBuffer): StartGameMessage {
            return StartGameMessage(b.getInt(), b.getInt())
        }

        override fun serialize(b: ByteBuffer, t: StartGameMessage) {
            IntAdapter.serialize(b, t.delay); IntAdapter.serialize(b, t.playerSize)
        }
    }
}
