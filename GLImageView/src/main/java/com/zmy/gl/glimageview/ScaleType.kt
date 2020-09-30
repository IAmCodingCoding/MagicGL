package com.zmy.gl.glimageview

import android.graphics.Matrix
import android.widget.ImageView

enum class ScaleType(val nativeInt: Int) {
    /**
     * Scale using the image matrix when drawing. The image matrix can be set using
     * [ImageView.setImageMatrix]. From XML, use this syntax:
     * `android:scaleType="matrix"`.
     */
    MATRIX(0),

    /**
     * Scale the image using [Matrix.ScaleToFit.FILL].
     * From XML, use this syntax: `android:scaleType="fitXY"`.
     */
    FIT_XY(1),

    /**
     * Scale the image using [Matrix.ScaleToFit.START].
     * From XML, use this syntax: `android:scaleType="fitStart"`.
     */
    FIT_START(2),

    /**
     * Scale the image using [Matrix.ScaleToFit.CENTER].
     * From XML, use this syntax:
     * `android:scaleType="fitCenter"`.
     */
    FIT_CENTER(3),

    /**
     * Scale the image using [Matrix.ScaleToFit.END].
     * From XML, use this syntax: `android:scaleType="fitEnd"`.
     */
    FIT_END(4),

    /**
     * Center the image in the view, but perform no scaling.
     * From XML, use this syntax: `android:scaleType="center"`.
     */
    CENTER(5),

    /**
     * Scale the image uniformly (maintain the image's aspect ratio) so
     * that both dimensions (width and height) of the image will be equal
     * to or larger than the corresponding dimension of the view
     * (minus padding). The image is then centered in the view.
     * From XML, use this syntax: `android:scaleType="centerCrop"`.
     */
    CENTER_CROP(6),

    /**
     * Scale the image uniformly (maintain the image's aspect ratio) so
     * that both dimensions (width and height) of the image will be equal
     * to or less than the corresponding dimension of the view
     * (minus padding). The image is then centered in the view.
     * From XML, use this syntax: `android:scaleType="centerInside"`.
     */
    CENTER_INSIDE(7);
}
