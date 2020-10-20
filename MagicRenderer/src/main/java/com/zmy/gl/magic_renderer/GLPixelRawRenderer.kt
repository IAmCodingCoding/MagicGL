package com.zmy.gl.magic_renderer

import android.opengl.GLES20.*
import java.nio.Buffer

open class GLPixelRawRenderer : GLTexture2DRenderer() {
    fun setImage(image: PixelData) {
        this.texture = image
    }
}

open class PixelData(
    private val buffer: Buffer?,
    private val format: Int,
    private val type: Int, width: Int, height: Int, needToStore: Boolean = true
) : TextureData(width, height, needToStore) {
    override fun initTexture() {
        glGenTextures(1, textures, 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D, 0)
        glUseProgram(0)
    }

    override fun upload() {
        if (!needToStore) return
        buffer?.let {
            glBindTexture(GL_TEXTURE_2D, textures[0])
            it.position(0)
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                format,
                width,
                height,
                0,
                format,
                type,
                buffer
            )
            glBindTexture(GL_TEXTURE_2D, 0)
        }
        needToStore = false
    }

    override fun bindTexture() {
        glBindTexture(GL_TEXTURE_2D, textures[0])
    }

}
