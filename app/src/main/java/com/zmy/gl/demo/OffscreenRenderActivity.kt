package com.zmy.gl.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zmy.gl.base.GLESVersion
import com.zmy.gl.base.GLThread
import com.zmy.gl.base.LogSwitch
import com.zmy.gl.base.egl.config.PBufferConfigChooser
import com.zmy.gl.base.egl.context.PBufferContextFactory
import com.zmy.gl.base.egl.surface.PBufferSurfaceFactory
import com.zmy.gl.magic_renderer.BitmapData
import com.zmy.gl.magic_renderer.GLPixelRawRenderer
import com.zmy.gl.magic_renderer.PixelData
import kotlinx.android.synthetic.main.activity_offscreen_render.*
import java.nio.ByteBuffer

class OffscreenRenderActivity : AppCompatActivity() {
    private lateinit var glThread: GLThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offscreen_render)
        var render = OffScreenRender()
        val imageStream = assets.open("test.jpg")
        val bm = BitmapFactory.decodeStream(imageStream)
        imageStream.close()
        val buffer = ByteBuffer.allocateDirect(bm.byteCount)
        bm.copyPixelsToBuffer(buffer)
        render.setImage(
            PixelData(
                buffer,
                BitmapData.getGLPixelFormat(bm),
                BitmapData.getGLPixelType(bm),
                bm.width,
                bm.height
            )
        )
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

    inner class OffScreenRender : GLPixelRawRenderer() {

        private val colorAttachment = intArrayOf(0)
        private val fbo = intArrayOf(0)
        override fun getVertexSrc(): String {
            return "#version 300 es\n" +
                    "layout(location=0) in vec4 aPosition;\n" +
                    "layout(location=1) in vec2 texturePosition;\n" +
                    "uniform mat4 trans;\n" +
                    "out vec2 tPosition;\n" +
                    "void main() {\n" +
                    "   tPosition = vec2(texturePosition.x,texturePosition.y);\n" +
                    "   gl_Position = trans  *  aPosition;\n" +
                    "}\n"
        }

        private fun initFrameBuffer() {
            GLES20.glGenFramebuffers(1, fbo, 0)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
            GLES20.glGenTextures(1, colorAttachment, 0)
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
                LogSwitch.d("zmy", "init frame buffer error")
            } else {
                LogSwitch.d("zmy", "init frame buffer success")
            }
        }

        override fun initTexture() {
            initFrameBuffer()
            super.initTexture()
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
                runOnUiThread {
                    image_view.setImageBitmap(bm)
                }
            }
        }
    }
}

