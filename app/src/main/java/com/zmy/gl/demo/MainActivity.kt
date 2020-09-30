package com.zmy.gl.demo


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.zmy.gl.glimageview.GLImageView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val src = arrayOf("test.png","test.jpg")
    private var count = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt.setOnClickListener {
            if (container.childCount > 0) {
                container.removeAllViews()
            } else {
                val imageView = GLImageView(this)
                val option=BitmapFactory.Options()
                option.inPreferredConfig = Bitmap.Config.RGB_565;
                val bm = BitmapFactory.decodeStream(assets.open(src[count++ % src.size]),null,option)
                imageView.setImageBitmap(bm!!)
                imageView.setBackgroundColor(Color.parseColor("#99000000"))
                container.addView(
                    imageView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }
    }
}