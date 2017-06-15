
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
data class PlayerCommand(
        val dest: Vector2?
) {
    companion object : KotlinSerializationAdapter<PlayerCommand>() {

        override fun parse(b: ByteBuffer): PlayerCommand {
            return PlayerCommand(vector2Adapter.nullAdapter.parse(b))
        }

        override fun serialize(b: ByteBuffer, t: PlayerCommand) {
            vector2Adapter.nullAdapter.serialize(b, t.dest)
        }
    }
}
data class PlayerCommandsMessage(
        val tick: Int,
        val commands: List<PlayerCommand>
) {
    companion object : KotlinSerializationAdapter<PlayerCommandsMessage>() {

        override fun parse(b: ByteBuffer): PlayerCommandsMessage {
            return PlayerCommandsMessage(b.getInt(), PlayerCommand.Companion.listAdapter.parse(b))
        }

        override fun serialize(b: ByteBuffer, t: PlayerCommandsMessage) {
            IntAdapter.serialize(b, t.tick); PlayerCommand.Companion.listAdapter.serialize(b, t.commands)
        }
    }
}
data class GameCommandsMessage(
        val tick: Int,
        val commands: List<List<PlayerCommand>>
) {
    companion object : KotlinSerializationAdapter<GameCommandsMessage>() {

        override fun parse(b: ByteBuffer): GameCommandsMessage {
            return GameCommandsMessage(b.getInt(), PlayerCommand.Companion.listAdapter.listAdapter.parse(b))
        }

        override fun serialize(b: ByteBuffer, t: GameCommandsMessage) {
            IntAdapter.serialize(b, t.tick); PlayerCommand.Companion.listAdapter.listAdapter.serialize(b, t.commands)
        }
    }
}
