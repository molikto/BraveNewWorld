
package org.snailya.bnw

import org.snailya.base.*
import java.nio.ByteBuffer
import com.badlogic.gdx.math.Vector2

data class StartGameMessage(
        val myIndex: Int,
        val delay: Int,
        val playerSize: Int
) {
    companion object : KotlinSerializationAdapter<StartGameMessage>() {

        override fun parse(b: ByteBuffer): StartGameMessage {
            return StartGameMessage(b.getInt(), b.getInt(), b.getInt())
        }

        override fun serialize(b: ByteBuffer, t: StartGameMessage) {
            IntAdapter.serialize(b, t.myIndex); IntAdapter.serialize(b, t.delay); IntAdapter.serialize(b, t.playerSize)
        }
    }
}
data class PlayerInput(
        val dest: Vector2?
) {
    companion object : KotlinSerializationAdapter<PlayerInput>() {

        override fun parse(b: ByteBuffer): PlayerInput {
            return PlayerInput(vector2Adapter.nullAdapter.parse(b))
        }

        override fun serialize(b: ByteBuffer, t: PlayerInput) {
            vector2Adapter.nullAdapter.serialize(b, t.dest)
        }
    }
}
data class PlayerInputMessage(
        val tick: Int,
        val inputs: List<PlayerInput>
) {
    companion object : KotlinSerializationAdapter<PlayerInputMessage>() {

        override fun parse(b: ByteBuffer): PlayerInputMessage {
            return PlayerInputMessage(b.getInt(), PlayerInput.Companion.listAdapter.parse(b))
        }

        override fun serialize(b: ByteBuffer, t: PlayerInputMessage) {
            IntAdapter.serialize(b, t.tick); PlayerInput.Companion.listAdapter.serialize(b, t.inputs)
        }
    }
}
data class PlayerInputsMessage(
        val tick: Int,
        val inputs: List<List<PlayerInput>>
) {
    companion object : KotlinSerializationAdapter<PlayerInputsMessage>() {

        override fun parse(b: ByteBuffer): PlayerInputsMessage {
            return PlayerInputsMessage(b.getInt(), PlayerInput.Companion.listAdapter.listAdapter.parse(b))
        }

        override fun serialize(b: ByteBuffer, t: PlayerInputsMessage) {
            IntAdapter.serialize(b, t.tick); PlayerInput.Companion.listAdapter.listAdapter.serialize(b, t.inputs)
        }
    }
}
