package com.zmy.gl.renders

import android.opengl.GLES20.*
import java.nio.Buffer

open class GLPixelRawRenderer : GLTextureRenderer() {
    fun setImage(image: PixelData) {
        this.image = image
    }
}

data class PixelData(
    private val buffer: Buffer,
    private val width: Int,
    private val height: Int,
    private val format: Int,
    private val type: Int
) : TextureData {
    override fun getWidth() = width

    override fun getHeight() = height

    override fun getFormat() = format

    override fun getType() = type

    override fun uploadToTexture(textures: IntArray) {
        glBindTexture(GL_TEXTURE_2D, textures[0])
        buffer.position(0)
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

}
