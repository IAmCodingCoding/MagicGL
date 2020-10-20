package com.zmy.gl.glimageview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.zmy.gl.base.GLESVersion
import com.zmy.gl.base.GLTextureView
import kotlin.math.min


internal class GLCoreImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GLTextureView(context, attrs, defStyleAttr) {
    private var render = com.zmy.gl.magic_renderer.GLBitmapRenderer()

    init {
        isOpaque = false
        glesVersion = GLESVersion.VERSION3X
        setRender(render)
    }

    override fun setBackground(background: Drawable?) {//disable the setBackground function
    }

    override fun setBackgroundColor(color: Int) {//disable the setBackground function
    }

    fun setGLBackgroundColor(color: Int) {
        render.backgroundColor = color
        requestRender()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = measureSize(widthMode, widthSize, render.getImageWidth())
        val height = measureSize(heightMode, heightSize, render.getImageHeight())
        setMeasuredDimension(width, height)
    }

    private fun measureSize(mode: Int, size: Int, imageSize: Int): Int {
        return when (mode) {
            MeasureSpec.AT_MOST -> min(imageSize, size)
            MeasureSpec.EXACTLY -> size
            else -> imageSize
        }
    }

    fun setImageBitmap(image: Bitmap) {
        render.setImage(image)
        requestRender()
    }

    fun setScaleType(type: com.zmy.gl.magic_renderer.ScaleType) {
        render.scaleType = type
        requestRender()
    }

    fun setRotate(degree: Float) {
        render.degree = degree
        requestRender()
    }
}