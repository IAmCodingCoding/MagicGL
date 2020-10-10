package com.zmy.gl.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gate.*

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gate)
        image.setOnClickListener {
            startActivity(Intent(this, GLImageViewActivity::class.java))
        }
        off_screen_render.setOnClickListener {
            startActivity(Intent(this, OffscreenRenderActivity::class.java))
        }
        render_yuv.setOnClickListener {
            startActivity(Intent(this, YUVActivity::class.java))
        }
    }
}