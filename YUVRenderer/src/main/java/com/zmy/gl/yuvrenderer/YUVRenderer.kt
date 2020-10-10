package com.zmy.gl.yuvrenderer

import android.graphics.Color
import android.opengl.EGLConfig
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES30.*
import android.opengl.Matrix
import android.util.Log
import com.zmy.gl.gltextureview.ConstantValue
import com.zmy.gl.gltextureview.render.GLBaseRenderer
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.properties.Delegates


class YUVRenderer : GLBaseRenderer() {

    companion object {
        private val TAG = YUVRenderer::class.java.simpleName
        private const val vertexSrc = "" +
                "#version 300 es\n" +
                "layout(location=0) in vec4 aPosition;\n" +
                "layout(location=1) in vec2 texturePosition;\n" +
                "out vec2 tPosition;\n" +
                "uniform mat4 trans;\n" +
                "void main() {\n" +
                "   tPosition = vec2(texturePosition.x,1.0-texturePosition.y);\n" +
                "   gl_Position =trans * aPosition;\n" +
                "}\n"
        private const val fragmentSrc = "#version 300 es\n" +
                " precision mediump float;\n" +
                " in vec2 tPosition;\n" +
                " out vec4 outColor;\n" +
                " uniform sampler2D texture_y;\n" +
                " uniform sampler2D texture_u;\n" +
                " uniform sampler2D texture_v;\n" +
                " void getRgbByYuv(in float y, in float u, in float v, inout float r, inout float g, inout float b){\n" +
                " \n" +
                "     y = 1.164*(y - 0.0625);\n" +
                "     u = u - 0.5;\n" +
                "     v = v - 0.5;\n" +
                "     r = y + 1.596023559570*v;\n" +
                "     g = y - 0.3917694091796875*u - 0.8129730224609375*v;\n" +
                "     b = y + 2.017227172851563*u;\n" +
                " }\n" +
                " void main() {\n" +
                " float r,g,b;\n" +
                "  float y = texture(texture_y, tPosition).r;\n" +
                "  float u = texture(texture_u, tPosition).r;\n" +
                "  float v = texture(texture_v, tPosition).r;\n" +
                " getRgbByYuv(y, u, v, r, g, b);\n" +
                " outColor = vec4(r,g,b,1.0); \n" +
                " }"
        private val textureName = arrayOf("texture_y", "texture_u", "texture_v");
        private val elementIndex = intArrayOf(0, 1, 2, 0, 2, 3)
        private val elementIndexBuffer =
            ByteBuffer.allocateDirect(elementIndex.size * ConstantValue.SIZE_OF_INT)
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
            ByteBuffer.allocateDirect(vertexData.size * ConstantValue.SIZE_OF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData).flip()
    }

    private var width: Int = 0
    private var height: Int = 0
    private val textures = intArrayOf(0, 0, 0)
    private var program: Int = 0
    private val vao = intArrayOf(0)
    private val vbo = intArrayOf(0, 0)

    var degree: Float by Delegates.observable(0f) { _, _, _ ->
        needStoreDegree = true
    }
        @Synchronized set
    private var needStoreDegree = true
        @Synchronized set
    var backgroundColor = Color.TRANSPARENT

    var image: YUVData? by Delegates.observable<YUVData?>(null) { _, _, _ ->
        needStoreImage = true
    }
        @Synchronized set
    private var needStoreImage = true
        @Synchronized set


    override fun onSurfaceCreated(config: EGLConfig?) {
        val vertexShader = createShader(
            GL_VERTEX_SHADER, vertexSrc
        )
        if (vertexShader == 0) {
            glDeleteShader(vertexShader)
            return
        }
        val fragmentShader = createShader(
            GL_FRAGMENT_SHADER, fragmentSrc
        )
        if (fragmentShader == 0) {
            glDeleteShader(fragmentShader)
            return
        }
        program = createProgram(vertexShader, fragmentShader)
        if (program == 0) {
            glDeleteProgram(program)
        }
        createTexture()
        initBuffers()
        needStoreDegree = true
        needStoreImage = true
    }

    open fun initBuffers() {
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

    private fun createTexture() {
        glUseProgram(program)
        glGenTextures(3, textures, 0)
        textures.forEachIndexed { index, texture ->
            glActiveTexture(GL_TEXTURE0 + index)
            glBindTexture(GL_TEXTURE_2D, textures[index])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            val textureLocation = glGetUniformLocation(program, textureName[index])
            glUniform1i(textureLocation, index)
        }
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    private fun uploadYUV() {
        image?.takeIf { needStoreImage }?.let {
            val data = arrayOf(it.y, it.u, it.v)
            val widthValue = arrayOf(it.width, it.width / 2, it.width / 2)
            val heightValue = arrayOf(it.height, it.height / 2, it.height / 2)
            data.forEachIndexed { index, buffer ->
                glBindTexture(GL_TEXTURE_2D, textures[index])
                glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_LUMINANCE,
                    widthValue[index],
                    heightValue[index],
                    0,
                    GL_LUMINANCE,
                    GL_UNSIGNED_BYTE,
                    buffer
                )
                val error = glGetError()
                if (error != GL_NO_ERROR) {
                    Log.d(TAG, "upload texture fail:${error}");
                }
            }
            needStoreImage=false
        }

    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onDrawFrame() {
        glViewport(0, 0, width, height)
        uploadYUV()
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
            glDrawElements(GL_TRIANGLES, elementIndex.size, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)
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

}

data class YUVData(val y: Buffer, val u: Buffer, val v: Buffer, val width: Int, val height: Int)