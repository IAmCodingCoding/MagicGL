package com.zmy.gl.glimageview;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.EGLConfig;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.zmy.gl.gltextureview.render.GLBaseRenderer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.opengl.GLES20.*;
import static android.opengl.GLES30.*;

public class GLBitmapRender1 extends GLBaseRenderer {

    private String TAG = GLBitmapRender.class.getSimpleName();
    private static final String vertexSrc = "#version 300 es\n" +
            "layout(location=0) in vec4 aPosition;\n" +
            "layout(location=1) in vec2 texturePosition;\n" +
            "uniform mat4 trans;\n" +
            "out vec2 tPosition;\n" +
            "void main() {\n" +
            "   tPosition = vec2(texturePosition.x,1.0f-texturePosition.y);\n" +
            "   gl_Position = trans  *  aPosition;\n" +
            "}\n";
    private static final String fragmentSrc = "#version 300 es\n" +
            "precision mediump float;\n" +
            "in vec2 tPosition;\n" +
            "out vec4 outColor;" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    outColor=texture(sTexture,tPosition);\n" +
            "}\n";
    private static final int SIZE_OF_FLOAT = 4;
    private static final int SIZE_OF_INT = 4;
    private static final int elementIndex[] = {0, 1, 2, 0, 2, 3};
    private static final Buffer elementIndexBuffer =
            ByteBuffer.allocateDirect(elementIndex.length * SIZE_OF_INT)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer()
                    .put(elementIndex).flip();


    private static final float vertexData[] = {
            1.0f, 1.0f, 1.0f,/*顶点坐标--右上*/  1.0f, 1.0f,/*纹理坐标--右上*/
            1.0f, -1.0f, 1.0f,/*顶点坐标--右下*/  1.0f, 0.0f,/*纹理坐标--右下*/
            -1.0f, -1.0f, 1.0f,/*顶点坐标--左下*/0.0f, 0.0f,/*纹理坐标--左下*/
            -1.0f, 1.0f, 1.0f,/*顶点坐标--左上*/  0.0f, 1.0f/*纹理坐标--左上*/
    };

    private static final Buffer vertexBuffer =
            ByteBuffer.allocateDirect(vertexData.length * SIZE_OF_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(vertexData).flip();
    private int texture[] = {0};
    private int program;
    private int fragmentShader;
    private int vertexShader;
    private int vao[] = {0};
    private int vbo[] = {0, 0};
    int backgroundColor = Color.TRANSPARENT;
    //    var image: Bitmap? by Delegates.observable<Bitmap?>(null) { _, _, _ ->
//            needStoreImage = true
//    }
    private boolean needStoreImage = true;
    private Bitmap image;

    public synchronized void setImage(Bitmap image) {
        needStoreImage = true;
        this.image = image;
    }


    float degree = 0f;
    private boolean needStoreDegree = true;

    public synchronized void setDegree(float degree) {
        needStoreDegree = true;
        this.degree = degree;
    }


    @Override
    public void onSurfaceCreated(EGLConfig config) {
        vertexShader = createShader(
                GL_VERTEX_SHADER, vertexSrc
        );
        if (vertexShader == 0) return;
        fragmentShader = createShader(
                GL_FRAGMENT_SHADER, fragmentSrc
        );
        if (fragmentShader == 0) return;
        program = createProgram(vertexShader, fragmentShader);
        if (program == 0) return;
        initBuffers();
        initTexture();
        needStoreDegree = true;
        needStoreImage = true;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        storeTexture();
        setTrans();
        glUseProgram(program);
        glClear(GL_COLOR_BUFFER_BIT);
        float r = Color.red(backgroundColor) / 255F;
        float g = Color.green(backgroundColor) / 255F;
        float b = Color.blue(backgroundColor) / 255F;
        float a = Color.alpha(backgroundColor) / 255F;
        glClearColor(r, g, b, a);
        glBindVertexArray(vao[0]);
        glBindTexture(GL_TEXTURE_2D, texture[0]);
        glDrawElements(GL_TRIANGLES, elementIndex.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    private void initBuffers() {
        glGenVertexArrays(1, vao, 0);
        glGenBuffers(2, vbo, 0);
        glBindVertexArray(vao[0]);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(
                GL_ARRAY_BUFFER,
                vertexData.length * SIZE_OF_FLOAT,
                vertexBuffer,
                GL_STATIC_DRAW
        );
        int aPosition = glGetAttribLocation(program, "aPosition");
        int texturePosition = glGetAttribLocation(program, "texturePosition");
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 5 * SIZE_OF_FLOAT, 0);
        glEnableVertexAttribArray(aPosition);
        glVertexAttribPointer(
                texturePosition,
                2,
                GL_FLOAT,
                false,
                5 * SIZE_OF_FLOAT,
                3 * SIZE_OF_FLOAT
        );
        glEnableVertexAttribArray(texturePosition);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[1]);
        glBufferData(
                GL_ELEMENT_ARRAY_BUFFER, elementIndex.length * SIZE_OF_INT, elementIndexBuffer,
                GL_STATIC_DRAW
        );
        glBindVertexArray(0);
    }
    private void initTexture() {
        glGenTextures(1, texture, 0);
        glBindTexture(GL_TEXTURE_2D, texture[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private synchronized void storeTexture() {
        if (image != null && needStoreImage) {
            glBindTexture(GL_TEXTURE_2D, texture[0]);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, image, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    private synchronized void setTrans() {
        if (degree >= 0 && needStoreDegree) {
            glUseProgram(program);
            int location = glGetUniformLocation(program, "trans");
            float matrix[] = new float[4 * 4];
            Matrix.setIdentityM(matrix, 0);
            Matrix.rotateM(matrix, 0, degree, 0f, 0f, -1f);
            glUniformMatrix4fv(location, 1, false, matrix, 0);
            glUseProgram(0);
        }
    }
}
