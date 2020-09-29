package com.zmy.gl.gltextureview.egl.context;

import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;

import com.zmy.gl.gltextureview.GLTextureView;

/**
 * An interface for customizing the eglCreateContext and eglDestroyContext calls.
 * <p>
 * This interface must be implemented by clients wishing to call
 * {@link GLTextureView#setEGLContextFactory(EGLContextFactory)}
 */
public interface EGLContextFactory {
    EGLContext createContext(EGLDisplay display, EGLConfig eglConfig);

    void destroyContext(EGLDisplay display, EGLContext context);
}