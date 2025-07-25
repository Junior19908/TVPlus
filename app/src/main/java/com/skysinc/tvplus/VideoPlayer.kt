package com.skysinc.tvplus

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoUrl: String
) {
    val context = LocalContext.current

    // Player e PlayerView (com remember para evitar recriação)
    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }
    var playerView: PlayerView? by remember { mutableStateOf(null) }

    // Cache simples de 100MB (mantido entre recomposições)
    val simpleCache = remember {
        val cacheDir = File(context.cacheDir, "media_cache")
        SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024))
    }

    // Configuração do player sempre que o videoUrl mudar
    LaunchedEffect(videoUrl) {
        exoPlayer?.release()

        // Configurações de buffering
        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                20_000,  // buffer mínimo (20s)
                60_000,  // buffer máximo (60s)
                2_000,   // buffer mínimo para iniciar
                3_000    // buffer mínimo para retomar
            )
            .build()

        // Fonte HTTP com redirecionamento permitido
        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        // Cache + DataSource
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpFactory)

        // Detecta tipo de mídia
        val mediaSource = if (videoUrl.endsWith(".m3u8")) {
            HlsMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrl))
        } else {
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUrl))
        }

        val player = ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
            .apply {
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        error.printStackTrace()
                    }
                })
            }

        exoPlayer = player
        playerView?.player = player
    }

    // Libera player quando Composable sai de tela
    DisposableEffect(videoUrl) {
        onDispose {
            releasePlayer(exoPlayer, playerView)
        }
    }

    // Renderização do PlayerView via AndroidView
    if (videoUrl.isNotEmpty()) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true // Controles visíveis
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }.also {
                    playerView = it
                }
            },
            update = { view ->
                if (view.player != exoPlayer) view.player = exoPlayer
            }
        )
    }
}

/** Libera o player e desconecta do PlayerView. */
private fun releasePlayer(player: ExoPlayer?, view: PlayerView?) {
    view?.player = null
    player?.release()
}

/** Limpa o cache do vídeo manualmente, se necessário. */
fun cleanVideoCache(context: android.content.Context) {
    val cacheDir = File(context.cacheDir, "media_cache")
    if (cacheDir.exists()) cacheDir.deleteRecursively()
}
