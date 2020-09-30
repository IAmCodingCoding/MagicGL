package com.zmy.gl.glimageview

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.EGLConfig
import android.opengl.GLES30.*
import android.opengl.GLUtils
import android.opengl.Matrix
import com.zmy.gl.gltextureview.render.GLBaseRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.properties.Delegates

class GLBitmapRender : GLBaseRenderer() {
    companion object {
        private val TAG = GLBitmapRender::class.java.simpleName
        private const val vertexSrc = "#version 300 es\n" +
                "layout(location=0) in vec4 aPosition;\n" +
                "layout(location=1) in vec2 texturePosition;\n" +
                "uniform mat4 trans;\n" +
                "out vec2 tPosition;\n" +
                "void main() {\n" +
                "   tPosition = vec2(texturePosition.x,1.0f-texturePosition.y);\n" +
                "   gl_Position = trans  *  aPosition;\n" +
                "}\n"
        private const val fragmentSrc = "#version 300 es\n" +
                "precision mediump float;\n" +
                "in vec2 tPosition;\n" +
                "out vec4 outColor;" +
                "uniform sampler2D sTexture;\n" +
                "void main() {\n" +
                "    outColor=texture(sTexture,tPosition);\n" +
                "}\n"
        private const val SIZE_OF_FLOAT = 4
        private const val SIZE_OF_INT = 4
        private val elementIndex = intArrayOf(0, 1, 2, 0, 2, 3)
        private val elementIndexBuffer =
            ByteBuffer.allocateDirect(elementIndex.size * SIZE_OF_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(elementIndex).flip()


        private val vertexData = floatArrayOf(
            1.0f, 1.0f, 1.0f,/*顶点坐标--右上*/  1.0f, 1.0f,/*纹理坐标--右上*/
            1.0f, -1.0f, 1.0f,/*顶点坐标--右下*/  1.0f, 0.0f,/*纹理坐标--右下*/
            -1.0f, -1.0f, 1.0f,/*顶点坐标--左下*/0.0f, 0.0f,/*纹理坐标--左下*/
            -1.0f, 1.0f, 1.0f,/*顶点坐标--左上*/  0.0f, 1.0f/*纹理坐标--左上*/
        )

        private val vertexBuffer =
            ByteBuffer.allocateDirect(vertexData.size * SIZE_OF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData).flip()
    }

    private var width: Int = 0
    private var height: Int = 0
    private val texture = intArrayOf(0)
    private var program: Int = 0
    private var fragmentShader = 0
    private var vertexShader = 0
    private val vao = intArrayOf(0)
    private val vbo = intArrayOf(0, 0)
    var backgroundColor = Color.TRANSPARENT

    var scaleType: ScaleType = ScaleType.FIT_CENTER
        @Synchronized set
    var image: Bitmap? by Delegates.observable<Bitmap?>(null) { _, _, _ ->
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
        glUseProgram(program)
        val r = Color.red(backgroundColor).toFloat() / 255F
        val g = Color.green(backgroundColor).toFloat() / 255F
        val b = Color.blue(backgroundColor).toFloat() / 255F
        val a = Color.alpha(backgroundColor).toFloat() / 255F
        glClearColor(r, g, b, a)
        glClear(GL_COLOR_BUFFER_BIT)
        image?.let {
            glBindVertexArray(vao[0])
            glBindTexture(GL_TEXTURE_2D, texture[0])
            glDrawElements(GL_TRIANGLES, elementIndex.size, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)
            glBindTexture(GL_TEXTURE_2D, 0)
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
            }
    }

    @Synchronized
    private fun storeTexture() {
        image?.takeIf { needStoreImage }
            ?.let {
                glBindTexture(GL_TEXTURE_2D, texture[0])
                GLUtils.texImage2D(GL_TEXTURE_2D, 0, it, 0)
                glBindTexture(GL_TEXTURE_2D, 0)
            }
    }

    fun getImageWidth() = image?.width ?: 0
    fun getImageHeight() = image?.height ?: 0

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    private fun handleScaleType() {
        image?.let {
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
                    glViewport(0, height-imageHeight, imageWidth, imageHeight)
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
        vertexShader = createShader(
            GL_VERTEX_SHADER, vertexSrc
        )
        if (vertexShader == 0) return
        fragmentShader = createShader(
            GL_FRAGMENT_SHADER, fragmentSrc
        )
        if (fragmentShader == 0) return
        program = createProgram(vertexShader, fragmentShader)
        if (program == 0) return
        initBuffers()
        initTexture()
        needStoreDegree = true
        needStoreImage = true
    }

    private fun initTexture() {
        glGenTextures(1, texture, 0)
        glBindTexture(GL_TEXTURE_2D, texture[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D, 0)
    }


    private fun initBuffers() {
        glGenVertexArrays(1, vao, 0)
        glGenBuffers(2, vbo, 0)
        glBindVertexArray(vao[0])
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
        glBufferData(
            GL_ARRAY_BUFFER,
            vertexData.size * SIZE_OF_FLOAT,
            vertexBuffer,
            GL_STATIC_DRAW
        )
        val aPosition = glGetAttribLocation(program, "aPosition")
        val texturePosition = glGetAttribLocation(program, "texturePosition")
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 5 * SIZE_OF_FLOAT, 0)
        glEnableVertexAttribArray(aPosition)
        glVertexAttribPointer(
            texturePosition,
            2,
            GL_FLOAT,
            false,
            5 * SIZE_OF_FLOAT,
            3 * SIZE_OF_FLOAT
        )
        glEnableVertexAttribArray(texturePosition)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[1])
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER, elementIndex.size * SIZE_OF_INT, elementIndexBuffer,
            GL_STATIC_DRAW
        )
        glBindVertexArray(0)
    }


}