package com.zmy.gl.base.egl.config;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;

import com.zmy.gl.base.GLESVersion;

public class ComponentSizeChooser extends BaseConfigChooser {
    // Subclasses can adjust these values:
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    private int[] mValue;

    public ComponentSizeChooser(GLESVersion eglVersion, int redSize, int greenSize, int blueSize,
                                int alphaSize, int depthSize, int stencilSize) {
        super(eglVersion, new int[]{
                EGL14.EGL_RED_SIZE, redSize,
                EGL14.EGL_GREEN_SIZE, greenSize,
                EGL14.EGL_BLUE_SIZE, blueSize,
                EGL14.EGL_ALPHA_SIZE, alphaSize,
                EGL14.EGL_DEPTH_SIZE, depthSize,
                EGL14.EGL_STENCIL_SIZE, stencilSize,
                EGL14.EGL_NONE});
        mValue = new int[1];
        mRedSize = redSize;
        mGreenSize = greenSize;
        mBlueSize = blueSize;
        mAlphaSize = alphaSize;
        mDepthSize = depthSize;
        mStencilSize = stencilSize;
    }

    @Override
    public EGLConfig chooseConfig(EGLDisplay display,
                                  EGLConfig[] configs) {
        for (EGLConfig config : configs) {
            int d = findConfigAttribute(display, config,
                    EGL14.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttribute(display, config,
                    EGL14.EGL_STENCIL_SIZE, 0);
            if ((d >= mDepthSize) && (s >= mStencilSize)) {
                int r = findConfigAttribute(display, config,
                        EGL14.EGL_RED_SIZE, 0);
                int g = findConfigAttribute(display, config,
                        EGL14.EGL_GREEN_SIZE, 0);
                int b = findConfigAttribute(display, config,
                        EGL14.EGL_BLUE_SIZE, 0);
                int a = findConfigAttribute(display, config,
                        EGL14.EGL_ALPHA_SIZE, 0);
                if ((r == mRedSize) && (g == mGreenSize)
                        && (b == mBlueSize) && (a == mAlphaSize)) {
                    return config;
                }
            }
        }
        return null;
    }

    private int findConfigAttribute(EGLDisplay display,
                                    EGLConfig config, int attribute, int defaultValue) {

        if (EGL14.eglGetConfigAttrib(display, config, attribute, mValue, 0)) {
            return mValue[0];
        }
        return defaultValue;
    }
}
