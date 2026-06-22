package com.ikanbot.tv

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BrowserHelperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        if (uri != null && uri.scheme != null) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "请在设备浏览器中打开以完成验证", Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }
}