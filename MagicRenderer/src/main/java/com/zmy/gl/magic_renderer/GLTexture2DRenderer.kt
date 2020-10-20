package com.zmy.gl.magic_renderer

import android.graphics.Color
import android.opengl.EGLConfig
import android.opengl.GLES30.*
import android.opengl.Matrix
import com.zmy.gl.base.ConstantValue
import com.zmy.gl.base.render.GLRectRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.properties.Delegates

open class GLTexture2DRenderer : GLRectRenderer() {
    private val TAG = GLTexture2DRenderer::class.java.simpleName
    private val textureCoordinate = floatArrayOf(
        1.0f, 1.0f,/*纹理坐标--右上*/
        1.0f, 0.0f,/*纹理坐标--右下*/
        0.0f, 0.0f,/*纹理坐标--左下*/
        0.0f, 1.0f/*纹理坐标--左上*/
    )

    private val textureCoordinateBuffer =
        ByteBuffer.allocateDirect(textureCoordinate.size * ConstantValue.SIZE_OF_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureCoordinate).flip()
    var backgroundColor = Color.TRANSPARENT

    var scaleType: ScaleType = ScaleType.FIT_CENTER
        @Synchronized set

    var texture: TextureData? = null

    var degree: Float by Delegates.observable(0f) { _, _, _ ->
        needStoreDegree = true
    }
        @Synchronized set
    private var needStoreDegree = true
        @Synchronized set

    override fun onDrawFrame() {
        handleScaleType()
        texture?.upload()
        setTrans()
        draw()
    }

    protected open fun draw() {
        val r = Color.red(backgroundColor).toFloat() / 255F
        val g = Color.green(backgroundColor).toFloat() / 255F
        val b = Color.blue(backgroundColor).toFloat() / 255F
        val a = Color.alpha(backgroundColor).toFloat() / 255F
        glClearColor(r, g, b, a)
        glClear(GL_COLOR_BUFFER_BIT)
        texture?.let {
            glUseProgram(program)
            glBindVertexArray(vao[0])
            it.bindTexture()
            glDrawElements(GL_TRIANGLES, elementIndex.size, GL_UNSIGNED_INT, 0)
            it.unbindTexture()
            glBindVertexArray(0)
            glUseProgram(0)
        }
    }

    @Synchronized
    private fun setTrans() {
        degree.takeIf { it >= 0 && needStoreDegree }
            ?.let {
                glUseProgram(program)
                val location = glGetUniformLocation(program, "trans")
                val matrix = FloatArray(4 * 4)
                Matrix.setIdentityM(matrix, 0)
                Matrix.rotateM(matrix, 0, it, 0f, 0f, -1f)
                glUniformMatrix4fv(location, 1, false, matrix, 0)
                glUseProgram(0)
                needStoreDegree = false
            }
    }


    fun getImageWidth() = texture?.width ?: 0

    fun getImageHeight() = texture?.height ?: 0

    protected fun handleScaleType() {
        texture?.let {
            val imageWidth = it.width
            val imageHeight = it.height
            when (scaleType) {
                ScaleType.FIT_XY -> glViewport(0, 0, width, height)
                ScaleType.FIT_CENTER -> {
                    if (imageWidth >= imageHeight) {
                        val scale = width.toFloat() / imageWidth
                        val scaleHeight = imageHeight * scale
                        glViewport(
                            0,
                            (height - scaleHeight).toInt() / 2,
                            width,
                            scaleHeight.toInt()
                        )
                    } else {
                        val scale = height.toFloat() / imageHeight
                        val scaleWidth = imageWidth * scale
                        glViewport(
                            (width - scaleWidth).toInt() / 2,
                            0,
                            scaleWidth.toInt(),
                            height
                        )
                    }
                }
                ScaleType.FIT_START -> {
                    if (imageWidth >= imageHeight) {
                        val scale = width.toFloat() / imageWidth
                        val scaleHeight = imageHeight * scale
                        glViewport(
                            0,
                            (height - scaleHeight).toInt(),
                            width,
                            scaleHeight.toInt()
                        )
                    } else {
                        val scale = height.toFloat() / imageHeight
                        val scaleWidth = imageWidth * scale
                        glViewport(
                            0,
                            0,
                            scaleWidth.toInt(),
                            height
                        )
                    }
                }
                ScaleType.FIT_END -> {
                    if (imageWidth >= imageHeight) {
                        val scale = width.toFloat() / imageWidth
                        val scaleHeight = imageHeight * scale
                        glViewport(
                            0,
                            0,
                            width,
                            scaleHeight.toInt()
                        )
                    } else {
                        val scale = height.toFloat() / imageHeight
                        val scaleWidth = imageWidth * scale
                        glViewport(
                            (width - scaleWidth).toInt(),
                            0,
                            scaleWidth.toInt(),
                            height
                        )
                    }
                }
                ScaleType.MATRIX -> {
                    glViewport(0, height - imageHeight, imageWidth, imageHeight)
                }
                ScaleType.CENTER -> {
                    glViewport(
                        (width - imageWidth) / 2,
                        (height - imageHeight) / 2,
                        imageWidth,
                        imageHeight
                    )
                }
                ScaleType.CENTER_CROP -> {
                    if (imageWidth <= imageHeight) {
                        val scale = width.toFloat() / imageWidth
                        val scaleHeight = imageHeight * scale
                        glViewport(
                            0,
                            (height - scaleHeight).toInt() / 2,
                            width,
                            scaleHeight.toInt()
                        )
                    } else {
                        val scale = height.toFloat() / imageHeight
                        val scaleWidth = imageWidth * scale
                        glViewport(
                            (width - scaleWidth).toInt() / 2,
                            0,
                            scaleWidth.toInt(),
                            height
                        )
                    }
                }
                ScaleType.CENTER_INSIDE -> {
                    if (imageWidth >= imageHeight) {
                        var scale = 1.0f
                        if (imageWidth > width) {
                            scale = width.toFloat() / imageWidth
                        }
                        val scaleWidth = imageWidth * scale
                        val scaleHeight = imageHeight * scale
                        glViewport(
                            (width - scaleWidth).toInt() / 2,
                            (height - scaleHeight).toInt() / 2,
                            scaleWidth.toInt(),
                            scaleHeight.toInt()
                        )
                    } else {
                        var scale = 1.0f
                        if (imageHeight > height) {
                            scale = height.toFloat() / imageHeight
                        }
                        val scaleWidth = imageWidth * scale
                        val scaleHeight = imageHeight * scale
                        glViewport(
                            (width - scaleWidth).toInt() / 2,
                            (height - scaleHeight).toInt() / 2,
                            scaleWidth.toInt(),
                            scaleHeight.toInt()
                        )
                    }
                }
            }
        }
    }

    override fun onSurfaceCreated(config: EGLConfig?) {
        super.onSurfaceCreated(config)
        initTexture()
        textureLinkToProgram()
        texture?.needToStore = true
        needStoreDegree = true
    }

    protected open fun initTexture() {
        texture?.initTexture()
    }

    protected open fun getTextureNames() = arrayOf("sTexture")

    protected open fun textureLinkToProgram() {
        texture?.let {
            val textureNames = getTextureNames()
            glUseProgram(program)
            it.textures.forEachIndexed { index, texture ->
                glActiveTexture(GL_TEXTURE0 + index)
                glBindTexture(GL_TEXTURE_2D, texture)
                val name = textureNames[index]
                val location = glGetUniformLocation(program, name)
                if (location >= 0) {
                    glUniform1i(location, index)
                }
            }
            glUseProgram(0)
            glBindTexture(GL_TEXTURE_2D, 0)
        }
    }

    override fun initBuffers() {
        super.initBuffers()
        glBindVertexArray(vao[0])
        val buffer = intArrayOf(0)
        glGenBuffers(1, buffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, buffer[0])
        glBufferData(
            GL_ARRAY_BUFFER,
            textureCoordinate.size * ConstantValue.SIZE_OF_FLOAT,
            textureCoordinateBuffer,
            GL_STATIC_DRAW
        )
        val location = glGetAttribLocation(program, "texturePosition")
        glVertexAttribPointer(
            location,
            2,
            GL_FLOAT,
            false,
            2 * ConstantValue.SIZE_OF_FLOAT,
            0
        )
        glEnableVertexAttribArray(location)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    override fun getFragmentSrc(): String {
        return "#version 300 es\n" +
                "precision mediump float;\n" +
                "in vec2 tPosition;\n" +
                "out vec4 outColor;" +
                "uniform sampler2D sTexture;\n" +
                "void main() {\n" +
                "    outColor=texture(sTexture,tPosition);\n" +
                "}\n"
    }

    override fun getVertexSrc(): String {
        return "#version 300 es\n" +
                "layout(location=0) in vec4 aPosition;\n" +
                "layout(location=1) in vec2 texturePosition;\n" +
                "uniform mat4 trans;\n" +
                "out vec2 tPosition;\n" +
                "void main() {\n" +
                "   tPosition = vec2(texturePosition.x,1.0f-texturePosition.y);\n" +
                "   gl_Position = trans  *  aPosition;\n" +
                "}\n"
    }
}

abstract class TextureData(val width: Int, val height: Int, var needToStore: Boolean = true) {
    var textures: IntArray = intArrayOf(0)
    abstract fun initTexture()

    abstract fun upload()

    abstract fun bindTexture()

    open fun unbindTexture() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}
