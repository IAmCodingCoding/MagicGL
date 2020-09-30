package com.zmy.gl.glimageview

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.FrameLayout

class GLImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var core= GLCoreImageView(context)

    init {
        initAttr(attrs)
        addView(core)
    }


    private fun initAttr(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GLImageView, 0, 0)
        val scaleType = ScaleType.values()[ta.getInt(
            R.styleable.GLImageView_android_scaleType,
            ScaleType.FIT_CENTER.nativeInt
        )]
        core.setScaleType(scaleType)
        ta.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        core.measure(widthMeasureSpec, heightMeasureSpec)
        val width = core.measuredWidth
        val height = core.measuredHeight
        setMeasuredDimension(width, height)
    }

    fun setImageBitmap(image: Bitmap) {
        core.setImageBitmap(image)
    }

    fun setScaleType(type: ScaleType) {
        core.setScaleType(type)

    }

    fun setRotate(degree: Float) {
        core.setRotate(degree)
    }


}