import java.io.File

abstract class KotlinType(open val name: String) {
    abstract fun adapter(): String
    open fun codegenParse(): String = "${adapter()}.parse(b)"
    open fun codegenSerialize(a: String): String = "${adapter()}.serialize(b, $a)"

    val q: KotlinType by lazy { NullableType(this) }
}

class CustomType(override val name: String, val adapterName: String) : KotlinType(name) {
    override fun adapter(): String = adapterName
}

object IntType : KotlinType("Int") {
    override fun adapter(): String = "IntAdapter"
    override fun codegenParse(): String = "b.getInt()"
}

object LongType : KotlinType("Long") {
    override fun adapter(): String = "LongAdapter"
    override fun codegenParse(): String = "b.getLong()"
}
object BooleanType : KotlinType("Boolean") {
    override fun adapter(): String = "BooleanAdapter"
    override fun codegenParse(): String = "(b.get() == 1.toByte())"
}
object StringType : KotlinType("String") {
    override fun adapter(): String = "StringAdapter"
}
class Field(val name: String, val ty: KotlinType) {
    fun  codegenDeclare(): String {
        return "val $name: ${ty.name}"
    }
}

class RecordType(override val name: String, val fields: List<Field>) : KotlinType(name) {
    override fun adapter(): String = "$name.Companion"

    fun codegen(): String {
        return """data class $name(
${fields.map { "        " +  it.codegenDeclare() }.joinToString(",\n")}
) {
    companion object : KotlinSerializationAdapter<$name>() {

        override fun parse(b: ByteBuffer): $name {
            return $name(${fields.map { it.ty.codegenParse() }.joinToString( ", ")})
        }

        override fun serialize(b: ByteBuffer, t: $name) {
            ${fields.map { it.ty.codegenSerialize( "t." + it.name) }.joinToString("; ")}
        }
    }
}
"""
    }
}
class ListType(val base: KotlinType) : KotlinType("List<${base.name}>") {
    override fun adapter(): String = "${base.adapter()}.listAdapter"
}

class NullableType(val base: KotlinType) : KotlinType("${base.name}?") {
    override fun adapter(): String = "${base.adapter()}.nullAdapter"
}

class EnumType(override val name: String, enums: List<String>) : KotlinType(name) {
    override fun adapter(): String = "$name.Companion"
}


open class Spec(
        val pkg: String,
        val dir: File
) {
    val records = mutableListOf<RecordType>()
    val enums = mutableListOf<EnumType>()

    fun record(name: String, vararg fs: Field): RecordType {
        val res = RecordType(name, fs.toList())
        records.add(res)
        return res
    }

    fun f(name: String, t: KotlinType) = Field(name, t)

    fun enum(name: String, vararg fs: String) {
        enums.add(EnumType(name, fs.toList()))
    }

    fun codegen() {
        val sb = StringBuilder()
        sb.append("""
package $pkg

import org.snailya.base.*
import java.nio.ByteBuffer
import com.badlogic.gdx.math.Vector2

""")
        for (c in records) sb.append(c.codegen())
        val f = File(dir, pkg.replace('.', '/') + "/KotlinGen.kt")
        println(f.absolutePath)
        if (f.exists()) f.delete()
        f.createNewFile()
        f.writeText(sb.toString())
    }
}


object BnwSpec : Spec("org.snailya.bnw", File("shared/src")) {


    val Vector2 = CustomType("Vector2", "Vector2Adapter")
    init {

        record("StartGameMessage",
                f("myIndex", IntType),
                f("serverTime", LongType),
                f("rtt", IntType),
                f("delay", IntType),
                f("playerSize", IntType)
        )

        val PlayerCommand = record("PlayerCommand",
                f("dest", Vector2.q)
        )

        val PlayerCommandsMessage = record("PlayerCommandsMessage",
                f("tick", IntType),
                f("debug_hash", IntType),
                //f("retry", BooleanType),
                f("commands", ListType(PlayerCommand)),
                f("debug_resend", BooleanType)
        )

        record("GameCommandsMessage",
                f("tick", IntType),
                f("commands", ListType(ListType(PlayerCommand))),
                f("debug_resend", BooleanType)
        )
    }
}

BnwSpec.codegen()
