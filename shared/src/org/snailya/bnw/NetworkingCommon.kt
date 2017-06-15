package org.snailya.bnw

import com.esotericsoftware.kryonet.*
import java.nio.ByteBuffer

/**
 * Created by molikto on 14/06/2017.
 */

data class StartGameMessage(
        @JvmField val delay: Int
)

class Tick {
}
object NetworkingCommon {
    const val tcpPort = 54554
    const val udpPort = 54552
    const val timePerTick = 100
    const val objectBufferSize = 2048
    const val writeBufferSize = objectBufferSize * 8

    @Suppress("UsePropertyAccessSyntax")
    object MySerialization : Serialization {
        override fun getLengthLength(): Int = 4

        override fun readLength(buffer: ByteBuffer): Int = buffer.getInt()
        override fun writeLength(buffer: ByteBuffer, length: Int) { buffer.putInt(length) }

        override fun write(connection: Connection, buffer: ByteBuffer, obj: Any) {
        }

        override fun read(connection: Connection, buffer: ByteBuffer): Any {
            return null
        }
    }

    fun  createClient(): Client = Client(writeBufferSize, objectBufferSize, MySerialization)

    fun  createServer(): Server = Server(writeBufferSize, objectBufferSize, MySerialization)
}

