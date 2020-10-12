package com.zmy.gl.yuvrenderer

import android.graphics.Color
import android.opengl.GLES30.*
import com.zmy.gl.renders.GLTextureRenderer
import com.zmy.gl.renders.TextureData
import java.nio.Buffer


class YUVRenderer : GLTextureRenderer() {

    private val TAG = YUVRenderer::class.java.simpleName

    private val textureName = arrayOf("texture_y", "texture_u", "texture_v");

    override fun draw() {
        glUseProgram(program)
        val r = Color.red(backgroundColor).toFloat() / 255F
        val g = Color.green(backgroundColor).toFloat() / 255F
        val b = Color.blue(backgroundColor).toFloat() / 255F
        val a = Color.alpha(backgroundColor).toFloat() / 255F
        glClearColor(r, g, b, a)
        glClear(GL_COLOR_BUFFER_BIT)
        image?.let {
            glBindVertexArray(vao[0])

            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, textures[0])
            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D, textures[1])
            glActiveTexture(GL_TEXTURE2)
            glBindTexture(GL_TEXTURE_2D, textures[2])

            glDrawElements(GL_TRIANGLES, elementIndex.size, GL_UNSIGNED_INT, 0)
            glBindVertexArray(0)
        }
    }

    override fun initTexture() {
        textures = intArrayOf(0, 0, 0)
        glUseProgram(program)
        glGenTextures(3, textures, 0)
        textures.forEachIndexed { index, texture ->
            glActiveTexture(GL_TEXTURE0 + index)
            glBindTexture(GL_TEXTURE_2D, texture)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            val textureLocation = glGetUniformLocation(program, textureName[index])
            glUniform1i(textureLocation, index)
        }
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun getVertexSrc(): String {
        return "#version 300 es\n" +
                "layout(location=0) in vec4 aPosition;\n" +
                "layout(location=1) in vec2 texturePosition;\n" +
                "out vec2 tPosition;\n" +
                "uniform mat4 trans;\n" +
                "void main() {\n" +
                "   tPosition = vec2(texturePosition.x,1.0-texturePosition.y);\n" +
                "   gl_Position =trans * aPosition;\n" +
                "}\n"
    }

    override fun getFragmentSrc(): String {
        return "#version 300 es\n" +
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
    }
}

data class YUVData(
    private val y: Buffer,
    private val u: Buffer,
    private val v: Buffer,
    private val width: Int,
    private val height: Int
) : TextureData {
    override fun getWidth() = width

    override fun getHeight() = height

    override fun getFormat() = GL_LUMINANCE

    override fun getType() = GL_UNSIGNED_BYTE

    override fun uploadToTexture(textures: IntArray) {
        val data = arrayOf(y, u, v)
        val widthValue = arrayOf(width, width / 2, width / 2)
        val heightValue = arrayOf(height, height / 2, height / 2)
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
            glBindTexture(GL_TEXTURE_2D, 0)
        }
    }

}