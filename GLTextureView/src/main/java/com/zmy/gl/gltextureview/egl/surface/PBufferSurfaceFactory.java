package com.zmy.gl.gltextureview.egl.surface;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;

public class PBufferSurfaceFactory extends DefaultWindowSurfaceFactory {
    private static String TAG = PBufferSurfaceFactory.class.getSimpleName();


    @Override
    public EGLSurface createWindowSurface(EGLDisplay display, EGLConfig config, Object nativeWindow) {
        EGLSurface result = null;
        try {
            result = EGL14.eglCreatePbufferSurface(display, config, null, 0);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return result;
    }
}
