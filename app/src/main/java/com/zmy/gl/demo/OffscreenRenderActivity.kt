package com.zmy.gl.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zmy.gl.base.GLESVersion
import com.zmy.gl.base.GLThread
import com.zmy.gl.base.egl.config.PBufferConfigChooser
import com.zmy.gl.base.egl.context.PBufferContextFactory
import com.zmy.gl.base.egl.surface.PBufferSurfaceFactory
import com.zmy.gl.glimageview.GLBitmapRenderer
import kotlinx.android.synthetic.main.activity_offscreen_render.*
import java.nio.ByteBuffer

class OffscreenRenderActivity : AppCompatActivity() {
    private lateinit var glThread: GLThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offscreen_render)
        var render = TestRender()
        val bm = BitmapFactory.decodeStream(assets.open("test.jpg"))
        render.setImage(bm)
        glThread = GLThread(
            null,
            PBufferConfigChooser(GLESVersion.VERSION3X, 8, 8, 8, 8, 16, 0),
            PBufferContextFactory(GLESVersion.VERSION3X),
            PBufferSurfaceFactory(),
            render,
            bm.width,
            bm.height
        )
        glThread.requestInitEgl(null, bm.width, bm.height)
    }

    override fun onDestroy() {
        super.onDestroy()
        glThread.requestDestroy()
    }

    inner class TestRender : GLBitmapRenderer() {

        private val colorAttachment = intArrayOf(0)
        private val fbo = intArrayOf(0)

        private fun initFrameBuffer() {
            GLES20.glGenFramebuffers(1, fbo, 0)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
            GLES20.glGenTextures(1, colorAttachment, 0)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, colorAttachment[0])
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                getImageWidth(),
                getImageHeight(),
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                colorAttachment[0],
                0
            )
            val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Log.d("zmy", "init frame buffer error")
            } else {
                Log.d("zmy", "init frame buffer success")
            }
        }


        override fun initTexture() {
            super.initTexture()
            initFrameBuffer()
        }

        override fun onDrawFrame() {
            super.onDrawFrame()
            val buffer = ByteBuffer.allocate(getImageWidth() * getImageHeight() * 4)
            GLES20.glReadPixels(
                0,
                0,
                getImageWidth(),
                getImageHeight(),
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                buffer
            )
            val bm = Bitmap.createBitmap(getImageWidth(), getImageHeight(), Bitmap.Config.ARGB_8888)
            bm.copyPixelsFromBuffer(buffer)
            if (!isDestroyed) {
                image_view.setImageBitmap(bm)
            }
        }
    }
}

