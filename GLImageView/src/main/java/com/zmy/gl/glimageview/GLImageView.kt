package com.zmy.gl.glimageview

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import com.zmy.gl.gltextureview.GLESVersion
import com.zmy.gl.gltextureview.GLTextureView
import com.zmy.gl.gltextureview.egl.config.RGBA8888EGLConfigChooser

class GLImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GLTextureView(context, attrs, defStyleAttr) {
    private var render = GLBitmapRender()

    init {
        isOpaque = false
        glesVersion = GLESVersion.VERSION3X
        setEGLConfigChooser(
            RGBA8888EGLConfigChooser(glesVersion, true)
        )
        setRender(render)
    }

    fun setImageBitmap(image: Bitmap) {
        render.image = image
        requestRender()
    }

    fun setRotate(degree: Float) {
        render.degree = degree
        requestRender()
    }


}