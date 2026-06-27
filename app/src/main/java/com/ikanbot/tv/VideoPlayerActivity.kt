package com.ikanbot.tv

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class VideoPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        playerView = findViewById(R.id.playerView)

        val videoUrl = intent.getStringExtra("video_url")
        if (videoUrl.isNullOrBlank()) {
            Toast.makeText(this, "无效的视频地址", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        player = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) finish()
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Toast.makeText(
                        this@VideoPlayerActivity,
                        "播放出错: ${error.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
        playerView.player = player
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        player?.run { stop(); release() }
        player = null
    }
}
