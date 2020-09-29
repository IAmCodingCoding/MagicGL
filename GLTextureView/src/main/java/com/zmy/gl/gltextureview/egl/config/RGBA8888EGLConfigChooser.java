package com.zmy.gl.gltextureview.egl.config;

import com.zmy.gl.gltextureview.GLESVersion;

public class RGBA8888EGLConfigChooser extends ComponentSizeChooser {
    public RGBA8888EGLConfigChooser(GLESVersion eglVersion, boolean withDepthBuffer) {
        super(eglVersion, 8, 8, 8, 8, withDepthBuffer ? 16 : 0, 0);
    }
}
