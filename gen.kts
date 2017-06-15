import java.io.File

abstract class KotlinType(open val name: String) {
    abstract fun adapter(): String
    open fun codegenParse(): String = "${adapter()}.parse(b)"
    open fun codegenSerialize(a: String): String = "${adapter()}.serialize(b, $a)"
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
    override fun codegenParse(): String = "(b.get() == 1)"
}
object StringType : KotlinType("String") {
    override fun adapter(): String = "StringAdapter"
}
class Field(val name: String, val ty: KotlinType) {
    fun  codegenDeclare(): String {
        return "val $name: ${ty.name}"
    }
}

class RecordType(override val name: String, val fields: List<Field>, val id: Int) : KotlinType(name) {
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

    fun record(name: String, vararg fs: Field) {
        records.add(RecordType(name, fs.toList()))
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

    init {
        record("StartGameMessage",
                f("delay", IntType),
                f("playerSize", IntType)
        )
    }
}

BnwSpec.codegen()
