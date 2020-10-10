package com.zmy.gl.base.egl.config;

import android.opengl.EGL14;

import com.zmy.gl.base.GLESVersion;

public class PBufferConfigChooser extends ComponentSizeChooser {

    public PBufferConfigChooser(GLESVersion eglVersion, int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        super(eglVersion, redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize);
    }

    @Override
    protected int[] filterConfigSpec(int[] configSpec) {
        int[] configs = super.filterConfigSpec(configSpec);
        int[] ret = new int[configs.length + 2];
        if (configs.length - 1 >= 0) System.arraycopy(configs, 0, ret, 0, configs.length - 1);
        ret[ret.length - 3] = EGL14.EGL_SURFACE_TYPE;
        ret[ret.length - 2] = EGL14.EGL_PBUFFER_BIT;
        ret[ret.length - 1] = EGL14.EGL_NONE;
        return ret;
    }
}
