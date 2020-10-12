package com.zmy.gl.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.zmy.gl.base.egl.EGLHelper;
import com.zmy.gl.base.egl.config.EGLConfigChooser;
import com.zmy.gl.base.egl.context.EGLContextFactory;
import com.zmy.gl.base.egl.surface.EGLWindowSurfaceFactory;
import com.zmy.gl.base.render.Renderer;

public class GLThread extends HandlerThread {
    private static final String TAG = GLThread.class.getSimpleName();
    public static final int MSG_INIT_EGL = 0;
    public static final int MSG_RENDER = 1;
    public static final int MSG_RESIZE = 2;
    public static final int MSG_DESTROY = 3;
    private EGLHelper mEglHelper;
    private volatile boolean isQuit;
    private Handler handler;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private Renderer render;


    public GLThread(Object nativeWindow, EGLConfigChooser mEGLConfigChooser,
                    EGLContextFactory mEGLContextFactory,
                    EGLWindowSurfaceFactory mEGLWindowSurfaceFactory,
                    Renderer render, int width, int height) {
        this("GLTexture_GLThread", nativeWindow, mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory, render, width, height);
    }

    public GLThread(String name, Object nativeWindow, EGLConfigChooser mEGLConfigChooser,
                    EGLContextFactory mEGLContextFactory,
                    EGLWindowSurfaceFactory mEGLWindowSurfaceFactory,
                    Renderer render, int width, int height) {
        super(name);
        this.render = render;
        this.mEGLConfigChooser = mEGLConfigChooser;
        this.mEGLContextFactory = mEGLContextFactory;
        this.mEGLWindowSurfaceFactory = mEGLWindowSurfaceFactory;
        start();
        initGLThread(nativeWindow, width, height);
    }

    public GLThread(Object nativeWindow, EGLConfigChooser mEGLConfigChooser,
                    EGLContextFactory mEGLContextFactory,
                    EGLWindowSurfaceFactory mEGLWindowSurfaceFactory,
                    Renderer render, int width, int height, int priority) {
        this("GLTexture_GLThread", nativeWindow, mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory, render, width, height, priority);
    }

    public GLThread(String name, Object nativeWindow, EGLConfigChooser mEGLConfigChooser,
                    EGLContextFactory mEGLContextFactory,
                    EGLWindowSurfaceFactory mEGLWindowSurfaceFactory,
                    Renderer render, int width, int height, int priority) {
        super(name, priority);
        this.render = render;
        this.mEGLConfigChooser = mEGLConfigChooser;
        this.mEGLContextFactory = mEGLContextFactory;
        this.mEGLWindowSurfaceFactory = mEGLWindowSurfaceFactory;
        start();
        initGLThread(nativeWindow, width, height);
    }

    private void initGLThread(Object nativeWindow, int width, int height) {
        mEglHelper = new EGLHelper(mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory);
        handler = new Handler(this.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_INIT_EGL:
                        mEglHelper.start();
                        mEglHelper.createSurface(msg.obj);
                        if (render != null) {
                            render.onSurfaceCreated(mEglHelper.getEglConfig());
                        }
                        if (render != null) {
                            render.onSurfaceChanged(msg.arg1, msg.arg2);
                        }
                    case MSG_RENDER:
                        if (render != null) {
                            render.onDrawFrame();
                            mEglHelper.swap();
                        }
                        break;
                    case MSG_RESIZE: {
                        mEglHelper.createSurface(msg.obj);
                        if (render != null) {
                            render.onSurfaceChanged(msg.arg1, msg.arg2);
                            render.onDrawFrame();
                            mEglHelper.swap();
                        }
                        break;
                    }
                    case MSG_DESTROY:
                        isQuit = true;
                        mEglHelper.destroySurface();
                        mEglHelper.finish();
                        getLooper().quit();
                        break;
                    default:
                        break;
                }
            }
        };
        if (nativeWindow == null) return;
        requestInitEgl(nativeWindow, width, height);
    }


    public void clear() {
        handler.removeCallbacksAndMessages(null);
    }

    public void requestInitEgl(Object nativeWindow, int width, int height) {
        sendMessageToHandler(MSG_INIT_EGL, width, height, nativeWindow, false);
    }
    public void requestResize(Object nativeWindow, int width, int height) {
        sendMessageToHandler(MSG_RESIZE,  width, height, nativeWindow, true);
    }
    public void requestRender() {
        sendMessageToHandler(MSG_RENDER, 0, 0, null, false);
    }

    public void requestDestroy() {
        sendMessageToHandler(MSG_DESTROY, 0, 0, null, true);
    }

    private void sendMessageToHandler(int what,
                                      int arg1, int arg2, Object obj, boolean isClear) {
        if (isQuit) return;
        if (isClear) clear();
        Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
    }

    @Override
    public void run() {
        super.run();
        if (LogSwitch.isLogOpened())
            Log.d(TAG, "GLThread :" + this.getName() + " exit!");
    }


}
