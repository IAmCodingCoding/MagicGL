package com.zmy.gl.base.egl.surface;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import com.zmy.gl.base.LogSwitch;

public class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
    private static final String TAG = DefaultWindowSurfaceFactory.class.getSimpleName();

    public EGLSurface createWindowSurface(EGLDisplay display,
                                          EGLConfig config, Object nativeWindow) {
        EGLSurface result = null;
        try {
            result = EGL14.eglCreateWindowSurface(display, config, nativeWindow, null, 0);
        } catch (IllegalArgumentException e) {
            LogSwitch.e(TAG,"fail to create Window surface :",e);
        }
        return result;
    }

    public void destroySurface(EGLDisplay display,
                               EGLSurface surface) {
        EGL14.eglDestroySurface(display, surface);
    }
}