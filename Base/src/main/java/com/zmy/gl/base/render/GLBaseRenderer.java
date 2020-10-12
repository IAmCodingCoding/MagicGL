package com.zmy.gl.base.render;

import com.zmy.gl.base.LogSwitch;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public abstract class GLBaseRenderer implements Renderer {
    private static final String TAG = GLBaseRenderer.class.getSimpleName();

    protected int createShader(int type, String src) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);
        int[] status = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            glDeleteShader(shader);
            LogSwitch.d(TAG, "create Shader error:${glGetShaderInfoLog(shader)}");
            return 0;
        }
        return shader;
    }

    protected int createProgram(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        int[] status = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, status, 0);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        if (status[0] == 0) {
            glDeleteProgram(program);
            LogSwitch.e(TAG, "program link  error:${glGetProgramInfoLog(program)}");
            return 0;
        }
        glValidateProgram(program);
        glGetProgramiv(program, GL_VALIDATE_STATUS, status, 0);
        if (status[0] == 0) {
            LogSwitch.e(TAG, "validate link  error:${glGetProgramInfoLog(program)}");
            glDeleteProgram(program);
            return 0;
        }

        return program;
    }
}
