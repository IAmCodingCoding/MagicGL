package com.zmy.gl.magic_renderer

import android.graphics.Bitmap
import android.opengl.GLES20.*
import android.opengl.GLUtils

open class GLBitmapRenderer : GLTexture2DRenderer() {
    fun setImage(bitmap: Bitmap) {
        this.texture = BitmapData(bitmap)
    }

}

class BitmapData(private val bitmap: Bitmap, needToStore: Boolean = true) :
    PixelData(null, 0, 0, bitmap.width, bitmap.height, needToStore) {
    companion object {
        fun getGLPixelFormat(bitmap: Bitmap): Int {
            return when (bitmap.config) {
                Bitmap.Config.ARGB_8888 -> GL_RGBA
                Bitmap.Config.ALPHA_8 -> GL_ALPHA
                Bitmap.Config.RGB_565 -> GL_RGB
                Bitmap.Config.ARGB_4444 -> GL_RGBA
                else -> 0
            }
        }

        fun getGLPixelType(bitmap: Bitmap): Int {
            return when (bitmap.config) {
                Bitmap.Config.ARGB_8888 -> GL_UNSIGNED_BYTE
                Bitmap.Config.ALPHA_8 -> GL_UNSIGNED_BYTE
                Bitmap.Config.RGB_565 -> GL_UNSIGNED_SHORT_5_6_5
                Bitmap.Config.ARGB_4444 -> GL_UNSIGNED_SHORT_4_4_4_4
                else -> 0
            }
        }
    }

    override fun upload() {
        if (!needToStore) return
        glBindTexture(GL_TEXTURE_2D, textures[0])
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, getGLPixelFormat(bitmap), bitmap, 0)
        glBindTexture(GL_TEXTURE_2D, 0)
        needToStore=false
    }
}