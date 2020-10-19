package com.zmy.gl.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zmy.gl.yuvrenderer.YUVData
import com.zmy.gl.yuvrenderer.YUVRenderer
import kotlinx.android.synthetic.main.activity_y_u_v.*
import java.nio.ByteBuffer

class YUVActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_y_u_v)
        val renderer = YUVRenderer()
        val width = 1920
        val height = 1080
        val y = ByteBuffer.allocateDirect(width * height)
        val u = ByteBuffer.allocateDirect(width * height / 4)
        val v = ByteBuffer.allocateDirect(width * height / 4)

        val data = YUVData(y, u, v, width, height)
        val array = ByteArray((width * height * 1.5).toInt());
        val fis = assets.open("I420_1920_1080.yuv")
        fis.read(array, 0, array.size)
        fis.close()
        y.put(array, 0, width * height)
        u.put(array, width * height, width * height / 4)
        v.put(array, width * height * 5 / 4, width * height / 4)

        y.flip()
        u.flip()
        v.flip()
        renderer.texture = data

        texture_view.setRender(renderer)
    }
}