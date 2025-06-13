package com.skysinc.tvplus

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.skysinc.tvplus.R

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val playerView = findViewById<PlayerView>(R.id.player_view)
        val channelUrl = intent.getStringExtra("CHANNEL_URL") ?: return

        player = ExoPlayer.Builder(this).build().also {
            playerView.player = it
            val mediaItem = MediaItem.fromUri(Uri.parse(channelUrl))
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
