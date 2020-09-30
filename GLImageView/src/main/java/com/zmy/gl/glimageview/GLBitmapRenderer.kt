package com.zmy.gl.glimageview

import android.graphics.Bitmap
import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLBitmapRenderer : GLImageRenderer() {

    fun setImage(bitmap: Bitmap) {
        val buffer = ByteBuffer.allocateDirect(bitmap.byteCount).order(ByteOrder.nativeOrder())
        bitmap.copyPixelsToBuffer(buffer)
        buffer.position(0)
        this.image = ImageData(buffer, bitmap.width, bitmap.height,getGLPixelFormat(bitmap),getGLPixelType(bitmap))
    }

    private fun getGLPixelFormat(bitmap: Bitmap): Int {
        return when (bitmap.config) {
            Bitmap.Config.ARGB_8888 -> GL_RGBA
            Bitmap.Config.ALPHA_8 -> GL_ALPHA
            Bitmap.Config.RGB_565 -> GL_RGB
            Bitmap.Config.ARGB_4444 -> GL_RGBA
            else -> 0
        }
    }
    private fun getGLPixelType(bitmap: Bitmap): Int {
        return when (bitmap.config) {
            Bitmap.Config.ARGB_8888 -> GL_UNSIGNED_BYTE
            Bitmap.Config.ALPHA_8 -> GL_UNSIGNED_BYTE
            Bitmap.Config.RGB_565 -> GL_UNSIGNED_SHORT_5_6_5
            Bitmap.Config.ARGB_4444 -> GL_UNSIGNED_SHORT_4_4_4_4
            else -> 0
        }
    }
}