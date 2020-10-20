package com.zmy.gl.magic_renderer

import android.opengl.GLES30.*
import java.nio.Buffer


class YUVRenderer : GLTexture2DRenderer() {

    private val TAG = YUVRenderer::class.java.simpleName


    override fun getTextureNames(): Array<String> {
        return arrayOf("texture_y", "texture_u", "texture_v");
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

class YUVData(
    private val y: Buffer,
    private val u: Buffer,
    private val v: Buffer, width: Int, height: Int, needToStore: Boolean = true,
) : TextureData(width, height, needToStore) {
    override fun initTexture() {
        textures = intArrayOf(0, 0, 0)
        glGenTextures(3, textures, 0)
        textures.forEachIndexed { index, texture ->
            glActiveTexture(GL_TEXTURE0 + index)
            glBindTexture(GL_TEXTURE_2D, texture)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        }
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun upload() {
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

    override fun bindTexture() {
        textures.forEachIndexed { index, texture ->
            glActiveTexture(GL_TEXTURE0 + index)
            glBindTexture(GL_TEXTURE_2D, texture)
        }
    }

}