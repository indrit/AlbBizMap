// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    var videoEnded by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    // Two independent effects below can each call onSplashFinished() — the normal
    // "video ended" path and the 8s fallback timeout — and if the video finishes
    // close to the 8s mark, both can fire. This is currently harmless only because
    // MainActivity's navigateSafe() happens to debounce the resulting double
    // navigation; guarding it here directly means that's no longer relied upon.
    var hasFinished by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(
                Uri.parse("asset:///MeTont_AnimationVid3.mp4")
            )
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    android.util.Log.d("SplashScreen", "State: $state")
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("SplashScreen", "Error: ${error.message}")
                }
            })
        }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    videoEnded = true
                }
            }
        })
    }

    LaunchedEffect(videoEnded) {
        if (videoEnded) {
            delay(500)
            if (!hasFinished) {
                hasFinished = true
                onSplashFinished()
            }
        }
    }

    // Fallback
    LaunchedEffect(Unit) {
        delay(8000)
        if (!hasFinished) {
            hasFinished = true
            onSplashFinished()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    if (!videoEnded) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setBackgroundColor(android.graphics.Color.parseColor("#E41E20"))
                    setShutterBackgroundColor(android.graphics.Color.parseColor("#E41E20"))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE41E20)),
            contentAlignment = Alignment.Center
        )  {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
            )
        }
    }
}