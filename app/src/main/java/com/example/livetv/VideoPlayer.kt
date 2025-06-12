package com.example.livetv

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoUrl: String
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    LaunchedEffect(videoUrl) { // Recria o player se a URL mudar
        exoPlayer?.release() // Libera o player anterior se existir
        val player = ExoPlayer.Builder(context).build().also {
            val mediaItem = MediaItem.fromUri(videoUrl)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = true // Começa a tocar automaticamente
        }
        exoPlayer = player
        playerView?.player = player // Atribui o novo player à PlayerView existente
    }

    DisposableEffect(Unit) {
        onDispose {
            playerView?.player = null
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    if (videoUrl.isNotEmpty()) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Configurações adicionais da PlayerView, se necessário
                    // useController = true // Para mostrar os controles padrão
                }.also {
                    playerView = it // Armazena a referência da PlayerView
                }
            },
            update = { view ->
                // O player é atualizado no LaunchedEffect quando exoPlayer muda
                // ou quando playerView é inicializado e exoPlayer já existe.
                if (view.player != exoPlayer) {
                    view.player = exoPlayer
                }
            }
        )
    }
}