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
    val vboBuffer = vbo.buffer
    val ibo = IndexBufferObject(staticIndices, maxIndices)
    val mesh: Mesh =  Reflect.on(Mesh::class.java).create(vbo, ibo, false).get<Mesh>()

    val bufferSize = attrs.vertexSize * maxVertices
    init {
        vbo.buffer
    }


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

    // private inlined
    var _index = 0
    val _cache  = FloatArray(bufferSize)

    inline fun put(a: Float) {
        if (_index >= _cache.size) {
            flush()
        }
        val index = _index
        _cache[index + 0] = a
        _index += 1
    }



    inline fun put(
            a0: Float,
            a1: Float,
            a2: Float,
            a3: Float,
            a4: Float,
            a5: Float,
            a6: Float,
            a7: Float,
            a8: Float
    ) {
        if (_index >= _cache.size) {
            flush()
        }
        val index = _index
        _cache[index + 0] = a0
        _cache[index + 1] = a1
        _cache[index + 2] = a2
        _cache[index + 3] = a3
        _cache[index + 4] = a4
        _cache[index + 5] = a5
        _cache[index + 6] = a6
        _cache[index + 7] = a7
        _cache[index + 8] = a8
        _index += 9
    }


    fun flush() {
        mesh.setVertices(_cache, 0, _index)
        _index = 0
        mesh.render(shader, primitiveType)
    }

    open fun render() {

    }
}

