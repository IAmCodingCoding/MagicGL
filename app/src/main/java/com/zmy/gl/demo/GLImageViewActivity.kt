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
import kotlinx.android.synthetic.main.activity_glimageview.*

class GLImageViewActivity : AppCompatActivity() {
    private val src = arrayOf("test.png","test.jpg")
    private var count = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glimageview)
        bt.setOnClickListener {
            if (container.childCount > 0) {
                container.removeAllViews()
                bt.text="SHOW IMAGE"
            } else {
                val imageView = GLImageView(this)
                val option = BitmapFactory.Options()
                option.inPreferredConfig = Bitmap.Config.RGB_565


                val imageStream = assets.open(src[count++ % src.size])
                val bm = BitmapFactory.decodeStream(imageStream, null, option)
                imageStream.close()
                imageView.setImageBitmap(bm!!)
                imageView.setBackgroundColor(Color.parseColor("#99000000"))
                container.addView(
                    imageView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                bt.text = "REMOVE IMAGE"
            }
        }
    }
}

