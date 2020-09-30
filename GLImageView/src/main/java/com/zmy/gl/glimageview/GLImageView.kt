package com.zmy.gl.glimageview

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import com.zmy.gl.gltextureview.GLESVersion
import com.zmy.gl.gltextureview.GLTextureView
import com.zmy.gl.gltextureview.egl.config.RGBA8888EGLConfigChooser
import kotlin.math.min


class GLImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GLTextureView(context, attrs, defStyleAttr) {
    private var render = GLBitmapRender()

    init {
        initAttr(attrs)
        isOpaque = false
        glesVersion = GLESVersion.VERSION3X
        setEGLConfigChooser(RGBA8888EGLConfigChooser(glesVersion, true))
        setRender(render)
    }

    private fun initAttr(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GLImageView, 0, 0)
        val scaleType = ScaleType.values()[ta.getInt(
            R.styleable.GLImageView_android_scaleType,
            ScaleType.FIT_CENTER.nativeInt
        )]
        render.scaleType = scaleType
        ta.recycle()
    }

    override fun setBackgroundColor(color: Int) {
        render.backgroundColor = color
        requestRender()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width = 0
        var height = 0
        when (widthMode) {
            MeasureSpec.AT_MOST -> width = min(render.getImageWidth(), widthSize)
            MeasureSpec.EXACTLY -> width = widthSize
            MeasureSpec.UNSPECIFIED -> width = render.getImageWidth()
        }
        when (heightMode) {
            MeasureSpec.AT_MOST -> height = min(render.getImageHeight(), heightSize)
            MeasureSpec.EXACTLY -> height = heightSize
            MeasureSpec.UNSPECIFIED -> height = render.getImageHeight()
        }
        setMeasuredDimension(width, height)
    }

    fun setImageBitmap(image: Bitmap) {
        render.image = image
        requestRender()
    }

    fun setScaleType(type: ScaleType) {
        render.scaleType = type
        requestRender()
    }

    fun setRotate(degree: Float) {
        render.degree = degree
        requestRender()
    }
}