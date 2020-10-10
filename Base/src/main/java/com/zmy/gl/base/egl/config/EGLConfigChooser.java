package com.zmy.gl.base.egl.config;

import android.opengl.EGLConfig;
import android.opengl.EGLDisplay;

import com.zmy.gl.base.GLTextureView;

/**
 * An interface for choosing an EGLConfig configuration from a list of
 * potential configurations.
 * <p>
 * This interface must be implemented by clients wishing to call
 * {@link GLTextureView#setEGLConfigChooser(EGLConfigChooser)}
 */
public interface EGLConfigChooser {
    /**
     * Choose a configuration from the list. Implementors typically
     * implement this method by calling
     * EGL specification available from The Khronos Group to learn how to call eglChooseConfig.
     *
     * @param display the current display.
     * @return the chosen configuration.
     */
    EGLConfig chooseConfig(EGLDisplay display);
}