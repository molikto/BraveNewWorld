package org.snailya.bnw

import com.esotericsoftware.kryonet.*
import org.snailya.base.KotlinSerializationAdapter
import org.snailya.bnw.NetworkingCommon.objectBufferSize
import org.snailya.bnw.NetworkingCommon.writeBufferSize
import java.nio.ByteBuffer



object NetworkingCommon {
    const val tcpPort = 54559
    const val udpPort = 54558
    const val timePerTick = 100
    const val timePerSimulation = 20
    const val objectBufferSize = 2048
    const val writeBufferSize = objectBufferSize * 8

    @Suppress("UsePropertyAccessSyntax")
    object MySerialization : Serialization {

        val frameworkMessages = listOf<Class<*>>(
                FrameworkMessage.RegisterTCP::class.java,
                FrameworkMessage.RegisterUDP::class.java,
                FrameworkMessage.KeepAlive::class.java,
                FrameworkMessage.DiscoverHost::class.java,
                FrameworkMessage.Ping::class.java
        )

        data class MyMessage<T>(val clazz: Class<T>, val parser: KotlinSerializationAdapter<T>)

        val myMessages = listOf<MyMessage<*>>(
                MyMessage(StartGameMessage::class.java, StartGameMessage.Companion),
                MyMessage(PlayerInputMessage::class.java, PlayerInputMessage.Companion),
                MyMessage(PlayerInputsMessage::class.java, PlayerInputsMessage.Companion)
        )

        override fun getLengthLength(): Int = 4
        override fun readLength(buffer: ByteBuffer): Int = buffer.getInt()
        override fun writeLength(buffer: ByteBuffer, length: Int) { buffer.putInt(length) }

        override fun write(connection: Connection?, b: ByteBuffer, obj: Any) {
            if (obj is FrameworkMessage.RegisterTCP) {
                b.put(0)
                b.putInt(obj.connectionID)
            } else if (obj is FrameworkMessage.RegisterUDP) {
                b.put(1)
                b.putInt(obj.connectionID)
            } else if (obj is FrameworkMessage.KeepAlive) {
                b.put(2)
            } else if (obj is FrameworkMessage.DiscoverHost) {
                b.put(3)
            } else if (obj is FrameworkMessage.Ping) {
                b.put(4)
                b.putInt(obj.id)
                b.put(if (obj.isReply) 1.toByte() else 0.toByte())
            } else {
                val index = myMessages.indexOfFirst{ it.clazz == obj.javaClass }
                b.put((index + 5).toByte())
                (myMessages[index].parser as KotlinSerializationAdapter<Any>).serialize(b, obj)
            }
        }

        override fun read(connection: Connection?, b: ByteBuffer): Any {
            val index = b.get()
            if (index == 0.toByte()) {
                val res = FrameworkMessage.RegisterTCP()
                res.connectionID = b.getInt()
                return res
            } else if (index == 1.toByte()) {
                val res = FrameworkMessage.RegisterUDP()
                res.connectionID = b.getInt()
                return res
            } else if (index == 2.toByte()) {
                val res = FrameworkMessage.keepAlive
                return res
            } else if (index == 3.toByte()) {
                val res = FrameworkMessage.DiscoverHost()
                return res
            } else if (index == 4.toByte()) {
                val res = FrameworkMessage.Ping()
                res.id = b.getInt()
                res.isReply = b.get() == 1.toByte()
                return res
            } else {
                return (myMessages[index - 5].parser as KotlinSerializationAdapter<Any>).parse(b)
            }
        }
    }

    fun  createClient(): Client = Client(writeBufferSize, objectBufferSize, MySerialization)

    fun  createServer(): Server = Server(writeBufferSize, objectBufferSize, MySerialization)
}

