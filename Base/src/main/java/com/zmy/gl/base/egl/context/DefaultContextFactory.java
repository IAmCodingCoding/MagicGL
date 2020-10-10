package com.zmy.gl.base.egl.context;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.util.Log;

import com.zmy.gl.base.GLESVersion;
import com.zmy.gl.base.egl.EGLHelper;

public class DefaultContextFactory implements EGLContextFactory {
    private final String TAG = DefaultContextFactory.class.getSimpleName();
    private GLESVersion mGLESVersion;

    public DefaultContextFactory(GLESVersion version) {
        this.mGLESVersion = version;
    }

    public EGLContext createContext(EGLDisplay display, EGLConfig config) {
        int[] attribute_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, mGLESVersion.getVersion(),
                EGL14.EGL_NONE};

        return EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT,
                mGLESVersion.getVersion() != 0 ? attribute_list : null, 0);
    }

    public void destroyContext(EGLDisplay display,
                               EGLContext context) {
        if (!EGL14.eglDestroyContext(display, context)) {
            Log.e(TAG, "display:" + display + " context: " + context + "tid=" + Thread.currentThread().getId());
            EGLHelper.throwEglException("eglDestroyContex", EGL14.eglGetError());
        }
    }
}