package com.ikanbot.tv

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@UnstableApi
class VideoPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingView: View
    private lateinit var errorView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        playerView = findViewById(R.id.playerView)
        loadingView = findViewById(R.id.loadingView)
        errorView = findViewById(R.id.errorView)

        val videoUrl = intent.getStringExtra("video_url")
        if (videoUrl.isNullOrBlank()) {
            showError("无效的视频链接")
            return
        }

        initializePlayer(videoUrl)
    }

    private fun initializePlayer(url: String) {
        player = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            loadingView.visibility = View.VISIBLE
                            findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE
                        }
                        Player.STATE_READY -> {
                            loadingView.visibility = View.GONE
                        }
                        Player.STATE_ENDED -> {
                            finish()
                        }
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    showError("播放失败: ${error.localizedMessage ?: "未知错误"}")
                }
            })
        }

        playerView.player = player
        playerView.useController = true
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
        playerView.controllerAutoShow = true
    }

    private fun showError(msg: String) {
        loadingView.visibility = View.GONE
        errorView.apply {
            visibility = View.VISIBLE
            text = msg
        }
        playerView.visibility = View.GONE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // TV remote: back to exit player
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
