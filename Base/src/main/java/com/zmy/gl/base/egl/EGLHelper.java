package com.zmy.gl.base.egl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;

import com.zmy.gl.base.LogSwitch;
import com.zmy.gl.base.egl.config.EGLConfigChooser;
import com.zmy.gl.base.egl.context.EGLContextFactory;
import com.zmy.gl.base.egl.surface.EGLWindowSurfaceFactory;

public class EGLHelper {
    private final String TAG = EGLHelper.class.getSimpleName();
    EGLDisplay mEglDisplay;
    EGLSurface mEglSurface;
    EGLConfig mEglConfig;
    EGLContext mEglContext;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;

    public EGLHelper(EGLConfigChooser mEGLConfigChooser,
                     EGLContextFactory mEGLContextFactory,
                     EGLWindowSurfaceFactory mEGLWindowSurfaceFactory) {
        this.mEGLConfigChooser = mEGLConfigChooser;
        this.mEGLContextFactory = mEGLContextFactory;
        this.mEGLWindowSurfaceFactory = mEGLWindowSurfaceFactory;
    }

    public static String formatEglError(String function, int error) {
        return function + " failed: " + getErrorString(error);
    }

    private static String getErrorString(int error) {
        switch (error) {
            case EGL14.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL14.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL14.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL14.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL14.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL14.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL14.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL14.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL14.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL14.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL14.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL14.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL14.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL14.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL14.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            default:
                return "0x" + error;
        }
    }

    public static void throwEglException(String function, int error) {
        String message = formatEglError(function, error);
        if (LogSwitch.isLogOpened()) {
            Log.e("EglHelper", "throwEglException tid=" + Thread.currentThread().getId() + " "
                    + message);
        }
        throw new RuntimeException(message);
    }

    public static void logEglErrorAsWarning(String tag, String function, int error) {
        Log.w(tag, formatEglError(function, error));
    }

    public EGLConfig getEglConfig() {
        return mEglConfig;
    }

    /**
     * Initialize EGL for a given configuration spec.
     *
     * @param
     */
    public void start() {
        if (LogSwitch.isLogOpened()) {
            Log.w(TAG, "start() tid=" + Thread.currentThread().getId());
        }

        /*
         * Get to the default display.
         */
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        /*
         * We can now initialize EGL for that display
         */
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }
        if (mEGLConfigChooser == null || mEGLContextFactory == null) {
            mEglConfig = null;
            mEglContext = null;
        } else {
            mEglConfig = mEGLConfigChooser.chooseConfig(mEglDisplay);

            /*
             * Create an EGL context. We want to do this as rarely as we can, because an
             * EGL context is a somewhat heavy object.
             */
            mEglContext = mEGLContextFactory.createContext(mEglDisplay, mEglConfig);
        }
        if (mEglContext == null || mEglContext == EGL14.EGL_NO_CONTEXT) {
            mEglContext = null;
            throwEglException("createContext");
        }
        if (LogSwitch.isLogOpened()) {
            Log.w(TAG, "createContext " + mEglContext + " tid=" + Thread.currentThread().getId());
        }

        mEglSurface = null;
    }

    /**
     * Create an egl surface for the current SurfaceHolder surface. If a surface
     * already exists, destroy it before creating the new surface.
     *
     * @return true if the surface was created successfully.
     */
    public boolean createSurface(Object nativeWindow) {
        if (LogSwitch.isLogOpened()) {
            Log.w(TAG, "createSurface()  tid=" + Thread.currentThread().getId());
        }
        if (mEglDisplay == null) {
            throw new RuntimeException("eglDisplay not initialized");
        }
        if (mEglConfig == null) {
            throw new RuntimeException("mEglConfig not initialized");
        }

        /*
         *  The window size has changed, so we need to create a new
         *  surface.
         */
        destroySurface();

        /*
         * Create an EGL surface we can render into.
         */
        if (mEGLWindowSurfaceFactory != null) {
            mEglSurface = mEGLWindowSurfaceFactory.createWindowSurface(
                    mEglDisplay, mEglConfig, nativeWindow);
        } else {
            mEglSurface = null;
        }

        if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
            int error = EGL14.eglGetError();
            if (error == EGL14.EGL_BAD_NATIVE_WINDOW) {
                Log.e(TAG, "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
            }
            return false;
        }

        /*
         * Before we can issue GL commands, we need to make sure
         * the context is current and bound to a surface.
         */
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            /*
             * Could not make the context current, probably because the underlying
             * SurfaceView surface has been destroyed.
             */
            logEglErrorAsWarning(TAG, "eglMakeCurrent", EGL14.eglGetError());
            return false;
        }

        return true;
    }

    /**
     * Display the current render surface.
     *
     * @return the EGL error code from eglSwapBuffers.
     */
    public int swap() {
        if (!EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)) {
            return EGL14.eglGetError();
        }
        return EGL14.EGL_SUCCESS;
    }

    public void destroySurface() {
        if (LogSwitch.isLogOpened()) {
            Log.w(TAG, "destroySurface()  tid=" + Thread.currentThread().getId());
        }
        if (mEglSurface != null && mEglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            if (mEGLWindowSurfaceFactory != null) {
                mEGLWindowSurfaceFactory.destroySurface(mEglDisplay, mEglSurface);
            }
            mEglSurface = null;
        }
    }

    public void finish() {
        if (LogSwitch.isLogOpened()) {
            Log.w(TAG, "finish() tid=" + Thread.currentThread().getId());
        }
        if (mEglContext != null) {
            if (mEGLContextFactory != null) {
                mEGLContextFactory.destroyContext(mEglDisplay, mEglContext);
            }
            mEglContext = null;
        }
        EGL14.eglReleaseThread();
        if (mEglDisplay != null) {
            EGL14.eglTerminate(mEglDisplay);
            mEglDisplay = null;
        }
    }

    private void throwEglException(String function) {
        throwEglException(function, EGL14.eglGetError());
    }
}
