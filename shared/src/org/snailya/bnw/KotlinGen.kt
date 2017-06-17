
package org.snailya.bnw

import org.snailya.base.*
import java.nio.ByteBuffer
import com.badlogic.gdx.math.Vector2

data class StartGameMessage(
        val myIndex: Int,
        val serverTime: Long,
        val rtt: Int,
        val delay: Int,
        val playerSize: Int
) {
    companion object : KotlinSerializationAdapter<StartGameMessage>() {

        override fun parse(b: ByteBuffer): StartGameMessage {
            return StartGameMessage(b.getInt(), b.getLong(), b.getInt(), b.getInt(), b.getInt())
        }

        override fun serialize(b: ByteBuffer, t: StartGameMessage) {
            IntAdapter.serialize(b, t.myIndex); LongAdapter.serialize(b, t.serverTime); IntAdapter.serialize(b, t.rtt); IntAdapter.serialize(b, t.delay); IntAdapter.serialize(b, t.playerSize)
        }
    }
}
data class PlayerCommand(
        val dest: Vector2?
) {
    companion object : KotlinSerializationAdapter<PlayerCommand>() {

        override fun parse(b: ByteBuffer): PlayerCommand {
            return PlayerCommand(Vector2Adapter.nullAdapter.parse(b))
        }

        override fun serialize(b: ByteBuffer, t: PlayerCommand) {
            Vector2Adapter.nullAdapter.serialize(b, t.dest)
        }
    }
}
data class PlayerCommandsMessage(
        val tick: Int,
        val debug_hash: Int,
        val commands: List<PlayerCommand>,
        val debug_resend: Boolean
) {
    companion object : KotlinSerializationAdapter<PlayerCommandsMessage>() {

        override fun parse(b: ByteBuffer): PlayerCommandsMessage {
            return PlayerCommandsMessage(b.getInt(), b.getInt(), PlayerCommand.Companion.listAdapter.parse(b), (b.get() == 1.toByte()))
        }

        override fun serialize(b: ByteBuffer, t: PlayerCommandsMessage) {
            IntAdapter.serialize(b, t.tick); IntAdapter.serialize(b, t.debug_hash); PlayerCommand.Companion.listAdapter.serialize(b, t.commands); BooleanAdapter.serialize(b, t.debug_resend)
        }
    }
}
data class GameCommandsMessage(
        val tick: Int,
        val commands: List<List<PlayerCommand>>,
        val debug_resend: Boolean
) {
    companion object : KotlinSerializationAdapter<GameCommandsMessage>() {

        override fun parse(b: ByteBuffer): GameCommandsMessage {
            return GameCommandsMessage(b.getInt(), PlayerCommand.Companion.listAdapter.listAdapter.parse(b), (b.get() == 1.toByte()))
        }

        override fun serialize(b: ByteBuffer, t: GameCommandsMessage) {
            IntAdapter.serialize(b, t.tick); PlayerCommand.Companion.listAdapter.listAdapter.serialize(b, t.commands); BooleanAdapter.serialize(b, t.debug_resend)
        }
    }
}
