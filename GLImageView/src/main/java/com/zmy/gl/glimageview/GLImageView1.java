package com.zmy.gl.glimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.zmy.gl.gltextureview.GLESVersion;
import com.zmy.gl.gltextureview.GLTextureView;
import com.zmy.gl.gltextureview.egl.config.RGBA8888EGLConfigChooser;


public class GLImageView1 extends GLTextureView {
//    private GLBitmapRender render = new GLBitmapRender();

    public GLImageView1(Context context) {
        super(context);
        initImageView();
    }

    public GLImageView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        initImageView();
    }

    public GLImageView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initImageView();
    }

    public GLImageView1(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initImageView();
    }

    private void initImageView() {
        setOpaque(false);
        setGLESVersion(GLESVersion.VERSION3X);
        setEGLConfigChooser(
                new RGBA8888EGLConfigChooser(getGLESVersion(), true)
        );
//        setRender(render);
    }

    public void setImageBitmap(Bitmap image) {
//        render.setImage(image);
        requestRender();
    }

    public void  setRotate(float degree) {
//        render.setDegree(degree);
        requestRender();
    }
}
