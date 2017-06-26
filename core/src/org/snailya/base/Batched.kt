package org.snailya.base

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GLTexture
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.glutils.IndexBufferObject
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO
import ktx.log.info
import org.joor.Reflect


open class Batched(
        val shader: ShaderProgram,
        attrs: VertexAttributes,
        maxVertices: Int,
        val texture: GLTexture,
        val primitiveType: Int = GL20.GL_TRIANGLES,
        //val textures: Array<GLTexture> = emptyArray(),
        staticIndices: Boolean = false,
        maxIndices: Int = 0,
        staticVertices: Boolean = false
) {
    val mesh: Mesh = Mesh(staticVertices, staticIndices, maxVertices, maxIndices, attrs)

    fun begin() {
        shader.begin()
        texture.bind()
//        for (i in 0 until textures.size) {
//            textures[i].bind(i)
//        }
    }

    fun end() {
        _flush()
        shader.end()
    }

    // private inlined
    var _index = 0
    val _cache  = FloatArray(attrs.vertexSize / 4 * maxVertices)

    inline fun put(a: Float) {
        if (_index == _cache.size) {
            _flush()
        }
        val index = _index
        _cache[index + 0] = a
        _index += 1
    }


    // TODO why cache??
    inline fun put(
            a0: Float,
            a1: Float,
            a2: Float
    ) {
        if (_index == _cache.size) {
            _flush()
        }
        val index = _index
        _cache[index + 0] = a0
        _cache[index + 1] = a1
        _cache[index + 2] = a2
        _index += 3
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
        if (_index == _cache.size) {
            _flush()
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


    fun _flush() {
        mesh.setVertices(_cache, 0, _index)
        _index = 0
        mesh.render(shader, primitiveType)
    }


    open fun render() {

    }
}

