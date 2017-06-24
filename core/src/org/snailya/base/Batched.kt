package org.snailya.base

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GLTexture
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.glutils.IndexBufferObject
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO
import com.sun.corba.se.impl.util.RepositoryId.cache
import com.sun.deploy.trace.Trace.flush
import org.joor.Reflect
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus


open class Batched(
        val shader: ShaderProgram,
        attrs: VertexAttributes,
        maxVertices: Int,
        val texture: GLTexture,
        val primitiveType: Int = GL20.GL_TRIANGLES,
        //val textures: Array<GLTexture> = emptyArray(),
        staticVertices: Boolean = false,
        staticIndices: Boolean = false,
        maxIndices: Int = 0
) {

    val vbo = VertexBufferObjectWithVAO(staticVertices, maxVertices, attrs)
    @JvmField val vboBuffer = vbo.buffer!!
    val ibo = IndexBufferObject(staticIndices, maxIndices)
    val mesh: Mesh =  Reflect.on(Mesh::class.java).create(vbo, ibo, false).get<Mesh>()



    fun begin() {
        shader.begin()
        texture.bind()
//        for (i in 0 until textures.size) {
//            textures[i].bind(i)
//        }
    }

    fun end() {
        flush()
        shader.end()
    }


    inline fun put(a: Float) {
        if (!vboBuffer.hasRemaining()) flush()
        vboBuffer.put(a)
    }


    fun flush() {
        mesh.render(shader, primitiveType)
        vboBuffer.position(0)
        vboBuffer.limit(vboBuffer.capacity())
    }

    open fun render() {

    }
}

