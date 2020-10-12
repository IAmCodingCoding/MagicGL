package com.zmy.gl.renders

import android.graphics.Color
import android.opengl.EGLConfig
import android.opengl.GLES30.*
import android.opengl.Matrix
import com.zmy.gl.base.ConstantValue
import com.zmy.gl.base.render.GLRectRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.properties.Delegates

open class GLTextureRenderer : GLRectRenderer() {
    private val TAG = GLTextureRenderer::class.java.simpleName
    private val vertexData = floatArrayOf(
        1.0f, 1.0f, 1.0f,/*顶点坐标--右上*/  1.0f, 1.0f,/*纹理坐标--右上*/
        1.0f, -1.0f, 1.0f,/*顶点坐标--右下*/  1.0f, 0.0f,/*纹理坐标--右下*/
        -1.0f, -1.0f, 1.0f,/*顶点坐标--左下*/0.0f, 0.0f,/*纹理坐标--左下*/
        -1.0f, 1.0f, 1.0f,/*顶点坐标--左上*/  0.0f, 1.0f/*纹理坐标--左上*/
    )

    private val vertexBuffer =
        ByteBuffer.allocateDirect(vertexData.size * ConstantValue.SIZE_OF_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData).flip()
    protected lateinit var textures: IntArray
    var backgroundColor = Color.TRANSPARENT

    var scaleType: ScaleType = ScaleType.FIT_CENTER
        @Synchronized set

    var image: TextureData? by Delegates.observable<TextureData?>(null) { _, _, _ ->
        needStoreImage = true
    }
        @Synchronized set
    private var needStoreImage = true
        @Synchronized set

    var degree: Float by Delegates.observable(0f) { _, _, _ ->
        needStoreDegree = true
    }
        @Synchronized set
    private var needStoreDegree = true
        @Synchronized set

    override fun onDrawFrame() {
        handleScaleType()
        storeTexture()
        setTrans()
        draw()
    }

    protected open fun draw() {
        glUseProgram(program)
        val r = Color.red(backgroundColor).toFloat() / 255F
        val g = Color.green(backgroundColor).toFloat() / 255F
        val b = Color.blue(backgroundColor).toFloat() / 255F
        val a = Color.alpha(backgroundColor).toFloat() / 255F
        glClearColor(r, g, b, a)
        glClear(GL_COLOR_BUFFER_BIT)
        image?.let {
            glBindVertexArray(vao[0])
            glBindTexture(GL_TEXTURE_2D, textures[0])
            glDrawElements(GL_TRIANGLES, elementIndex.size, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)
            glBindTexture(GL_TEXTURE_2D, 0)
        }
        glUseProgram(0)
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

    @Synchronized
    private fun storeTexture() {
        image?.takeIf { needStoreImage }
            ?.let {
                if (it.getFormat() != 0) {
                    it.uploadToTexture(textures)
                }
                needStoreImage = false
            }
    }

    fun getImageWidth() = image?.getWidth() ?: 0
    fun getImageHeight() = image?.getHeight() ?: 0

    protected fun handleScaleType() {
        image?.let {
            val imageWidth = it.getWidth()
            val imageHeight = it.getHeight()
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
        needStoreDegree = true
        needStoreImage = true
    }

    open fun initTexture() {
        textures = intArrayOf(0)
        glUseProgram(program)
        glGenTextures(1, textures, 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textures[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        val textureLocation = glGetUniformLocation(program, "sTexture")
        glUniform1i(textureLocation, 0)
        glBindTexture(GL_TEXTURE_2D, 0)
        glUseProgram(0)
    }

    override fun initBuffers() {
        glGenVertexArrays(1, vao, 0)
        glGenBuffers(2, vbo, 0)
        glBindVertexArray(vao[0])
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(
            GL_ARRAY_BUFFER,
            vertexData.size * ConstantValue.SIZE_OF_FLOAT,
            vertexBuffer,
            GL_STATIC_DRAW
        )
        val aPosition = glGetAttribLocation(program, "aPosition")
        val texturePosition = glGetAttribLocation(program, "texturePosition")
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 5 * ConstantValue.SIZE_OF_FLOAT, 0)
        glEnableVertexAttribArray(aPosition)
        glVertexAttribPointer(
            texturePosition,
            2,
            GL_FLOAT,
            false,
            5 * ConstantValue.SIZE_OF_FLOAT,
            3 * ConstantValue.SIZE_OF_FLOAT
        )
        glEnableVertexAttribArray(texturePosition)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[1])
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            elementIndex.size * ConstantValue.SIZE_OF_INT,
            elementIndexBuffer,
            GL_STATIC_DRAW
        )
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

interface TextureData {
    fun getWidth(): Int
    fun getHeight(): Int
    fun getFormat(): Int
    fun getType(): Int
    fun uploadToTexture(textures: IntArray)
}
