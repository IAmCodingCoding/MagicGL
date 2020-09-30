package com.zmy.gl.gltextureview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.zmy.gl.gltextureview.egl.config.EGLConfigChooser;
import com.zmy.gl.gltextureview.egl.config.RGBA8888EGLConfigChooser;
import com.zmy.gl.gltextureview.egl.context.DefaultContextFactory;
import com.zmy.gl.gltextureview.egl.context.EGLContextFactory;
import com.zmy.gl.gltextureview.egl.surface.DefaultWindowSurfaceFactory;
import com.zmy.gl.gltextureview.egl.surface.EGLWindowSurfaceFactory;
import com.zmy.gl.gltextureview.render.Renderer;

public class GLTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = GLTextureView.class.getSimpleName();

    private GLThread glThread;

    private GLESVersion mGLESVersion = GLESVersion.VERSION2X;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private Renderer render;


    public GLTextureView(Context context) {
        super(context);
        init();
    }

    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GLTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public GLESVersion getGLESVersion() {
        return mGLESVersion;
    }

    public void setGLESVersion(GLESVersion version) {
        mGLESVersion = version;
        reInitEGL();
    }

    private void reInitEGL() {
        if (glThread != null) {
            glThread.requestDestroy();
            glThread = null;
        }
        if (render != null && getSurfaceTexture() != null) {
            glThread = new GLThread(new Surface(getSurfaceTexture()), mEGLConfigChooser, mEGLContextFactory
                    , mEGLWindowSurfaceFactory, render, getMeasuredWidth()
                    , getMeasuredHeight());
        }

    }

    public void setEGLConfigChooser(@NonNull EGLConfigChooser mEGLConfigChooser) {
        this.mEGLConfigChooser = mEGLConfigChooser;
        reInitEGL();
    }

    public void setEGLContextFactory(@NonNull EGLContextFactory mEGLContextFactory) {
        this.mEGLContextFactory = mEGLContextFactory;
        reInitEGL();
    }

    public void setEGLWindowSurfaceFactory(@NonNull EGLWindowSurfaceFactory mEGLWindowSurfaceFactory) {
        this.mEGLWindowSurfaceFactory = mEGLWindowSurfaceFactory;
        if (glThread != null) {
            glThread.requestDestroy();
            glThread = null;
        }
        if (render != null && getSurfaceTexture() != null) {
            glThread = new GLThread(new Surface(getSurfaceTexture()), mEGLConfigChooser, mEGLContextFactory
                    , mEGLWindowSurfaceFactory, render, getMeasuredWidth()
                    , getMeasuredHeight());
        }
    }


    private void checkRenderThreadState() {
        if (glThread != null) {
            throw new IllegalStateException(
                    "setRenderer has already been called for this instance.");
        }
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    public void setRender(Renderer render) {
        this.render = render;
        if (mEGLConfigChooser == null) {
            mEGLConfigChooser = new RGBA8888EGLConfigChooser(mGLESVersion, true);
        }
        if (mEGLContextFactory == null) {
            mEGLContextFactory = new DefaultContextFactory(mGLESVersion);
        }
        if (mEGLWindowSurfaceFactory == null) {
            mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        SurfaceTexture surfaceTexture = getSurfaceTexture();
        if (surfaceTexture != null && glThread == null) {
            glThread = new GLThread(new Surface(surfaceTexture), mEGLConfigChooser, mEGLContextFactory
                    , mEGLWindowSurfaceFactory, render, getMeasuredWidth()
                    , getMeasuredHeight());
        }
    }

    public void requestRender() {
        if (render != null && glThread != null) {
            glThread.requestRender();
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (LogSwitch.isLogOpened())
            Log.d(TAG, "onSurfaceTextureAvailable");
        if (glThread == null)
            glThread = new GLThread(new Surface(surface), mEGLConfigChooser, mEGLContextFactory
                    , mEGLWindowSurfaceFactory, render, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (LogSwitch.isLogOpened())
            Log.d(TAG, "onSurfaceTextureSizeChanged");
        glThread.requestResize(width, height);
        glThread.requestRender();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (LogSwitch.isLogOpened())
            Log.d(TAG, "onSurfaceTextureDestroyed");
        glThread.requestDestroy();
        glThread = null;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (LogSwitch.isLogOpened())
            Log.d(TAG, "onSurfaceTextureUpdated");
    }

}
