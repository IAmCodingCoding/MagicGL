package com.zmy.gl.base.egl.config;

import com.zmy.gl.base.GLESVersion;

public class RGB888EGLConfigChooser extends ComponentSizeChooser {
    public RGB888EGLConfigChooser(GLESVersion eglVersion, boolean withDepthBuffer) {
        super(eglVersion, 8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0);
    }
}
