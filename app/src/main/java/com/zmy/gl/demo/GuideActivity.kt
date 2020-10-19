package com.zmy.gl.demo

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gate.*

class GuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gate)
        image.setOnClickListener {
            startActivity(
                Intent(this, GLImageViewActivity::class.java)
            )
        }
        off_screen_render.setOnClickListener {
            startActivity(
                Intent(this, OffscreenRenderActivity::class.java)
            )
        }
        render_yuv.setOnClickListener {
            startActivity(Intent(this, YUVActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}