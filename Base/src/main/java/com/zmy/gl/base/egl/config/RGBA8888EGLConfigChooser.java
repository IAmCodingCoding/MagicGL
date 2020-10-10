package com.zmy.gl.base.egl.config;

import com.zmy.gl.base.GLESVersion;

public class RGBA8888EGLConfigChooser extends ComponentSizeChooser {
    public RGBA8888EGLConfigChooser(GLESVersion eglVersion, boolean withDepthBuffer) {
        super(eglVersion, 8, 8, 8, 8, withDepthBuffer ? 16 : 0, 0);
    }
}
