package com.zmy.gl.demo


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_glimageview.*

class GLImageViewActivity : AppCompatActivity() {
    private val src = arrayOf("test.png", "test.jpg")

    companion object {
        private var count = 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glimageview)
        val option = BitmapFactory.Options()
        option.inPreferredConfig = Bitmap.Config.RGB_565

        val imageStream = assets.open(src[count++ % src.size])
        val bm = BitmapFactory.decodeStream(imageStream, null, option)
        imageStream.close()
        image.setImageBitmap(bm!!)
        image.setBackgroundColor(Color.parseColor("#99000000"))
    }
}

