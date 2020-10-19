package com.zmy.gl.base.render

import android.opengl.EGLConfig
import android.opengl.GLES30.*
import com.zmy.gl.base.ConstantValue
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class GLRectRenderer : GLBaseRenderer() {
    protected val elementIndex = intArrayOf(0, 1, 2, 0, 2, 3)
    protected val elementIndexBuffer =
        ByteBuffer.allocateDirect(elementIndex.size * ConstantValue.SIZE_OF_INT)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(elementIndex).flip()


    private val vertexData = floatArrayOf(
        1.0f, 1.0f, 1.0f,/*顶点坐标--右上*/
        1.0f, -1.0f, 1.0f,/*顶点坐标--右下*/
        -1.0f, -1.0f, 1.0f,/*顶点坐标--左下*/
        -1.0f, 1.0f, 1.0f,/*顶点坐标--左上*/
    )


    private val vertexBuffer =
        ByteBuffer.allocateDirect(vertexData.size * ConstantValue.SIZE_OF_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData).flip()
    protected var width: Int = 0
    protected var height: Int = 0
    protected var program: Int = 0
    protected var fragmentShader = 0
    protected var vertexShader = 0
    protected val vao = intArrayOf(0)
    protected val vbo = intArrayOf(0, 0)

    open fun getVertexSrc(): String {
        return "#version 300 es\n" +
                "layout(location=0) in vec4 aPosition;\n" +
                "out vec2 tPosition;\n" +
                "void main() {\n" +
                "   gl_Position = aPosition;\n" +
                "}\n"
    }

    open fun getFragmentSrc(): String {
        return "#version 300 es\n" +
                "precision mediump float;\n" +
                "in vec2 tPosition;\n" +
                "out vec4 outColor;" +
                "void main() {\n" +
                "    outColor=vec4(1.0f, 0.0f, 1.0f, 1.0f);\n" +
                "}\n"
    }


    override fun onSurfaceCreated(config: EGLConfig?) {
        vertexShader = createShader(
            GL_VERTEX_SHADER, getVertexSrc()
        )
        if (vertexShader == 0) return
        fragmentShader = createShader(
            GL_FRAGMENT_SHADER, getFragmentSrc()
        )
        if (fragmentShader == 0) return
        program = createProgram(vertexShader, fragmentShader)
        if (program == 0) return
        initBuffers()
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
        val vertexCoordinate = glGetAttribLocation(program, "aPosition")
        glVertexAttribPointer(
            vertexCoordinate,
            3,
            GL_FLOAT,
            false,
            3 * ConstantValue.SIZE_OF_FLOAT,
            0
        )
        glEnableVertexAttribArray(vertexCoordinate)
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

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onDrawFrame() {
        glViewport(0, 0, width, height)
        glUseProgram(program)
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        glBindVertexArray(vao[0])
        glDrawElements(GL_TRIANGLES, elementIndex.size, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }

}