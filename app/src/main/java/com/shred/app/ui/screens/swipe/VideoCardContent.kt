package com.shred.app.ui.screens.swipe

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.shred.app.data.model.MediaFile
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter
import kotlinx.coroutines.delay
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun VideoCardContent(
    file: MediaFile,
    isZoomMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalShredColors.current

    var isPlaying by remember { mutableStateOf(false) }
    var isFullscreenScrubber by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var hasError by remember { mutableStateOf(false) }

    val exoPlayer = remember(file.path) {
        try {
            val f = File(file.path)
            if (!f.exists() || f.length() <= 0) {
                hasError = true
                null
            } else {
                ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.fromFile(f))
                    setMediaItem(mediaItem)
                    setHandleAudioBecomingNoisy(true)
                    addListener(object : androidx.media3.common.Player.Listener {
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.e("VideoCardContent", "ExoPlayer error for ${file.fileName}: ${error.message}", error)
                            isPlaying = false
                            hasError = true
                        }
                    })
                    // NOTE: prepare() is called lazily on first play tap (not here)
                    // to avoid buffering all videos in the stack simultaneously
                    playWhenReady = false
                }
            }
        } catch (e: Exception) {
            Log.e("VideoCardContent", "ExoPlayer creation failed for ${file.fileName}", e)
            hasError = true
            null
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            try {
                exoPlayer?.stop()
                exoPlayer?.release()
            } catch (e: Exception) {
                Log.w("VideoCardContent", "Error releasing ExoPlayer", e)
            }
        }
    }

    // Polling player position for scrubber
    LaunchedEffect(isPlaying) {
        val player = exoPlayer ?: return@LaunchedEffect
        while (isPlaying && !hasError) {
            try {
                currentPositionMs = player.currentPosition
                durationMs = player.duration.coerceAtLeast(1L)
            } catch (e: Exception) {
                Log.e("VideoCardContent", "Video polling error", e)
                isPlaying = false
                hasError = true
                break
            }
            delay(200)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (exoPlayer == null || hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.cardBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ UNPLAYABLE VIDEO / FILE MOVED ]\n${file.fileName}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.accent
                )
            }
        } else {
            ZoomableBox(
                isZoomMode = isZoomMode,
                modifier = Modifier.fillMaxSize()
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Tap overlay to play / pause
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (!isZoomMode && !hasError) {
                            try {
                                if (exoPlayer?.isPlaying == true) {
                                    exoPlayer.pause()
                                    isPlaying = false
                                } else {
                                    if (exoPlayer?.playbackState == androidx.media3.common.Player.STATE_IDLE) {
                                        exoPlayer.prepare()
                                    }
                                    exoPlayer?.play()
                                    isPlaying = true
                                }
                            } catch (e: Exception) {
                                Log.e("VideoCardContent", "Play/pause error", e)
                                isPlaying = false
                                hasError = true
                            }
                        }
                    }
            )
        }

        // Play icon overlay when paused
        if (!isPlaying && !isZoomMode) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(2.dp, colors.border),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Bottom Bar (Duration & Fullscreen toggle)
        if (!isZoomMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isFullscreenScrubber && durationMs > 0) {
                    Slider(
                        value = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f),
                        onValueChange = { fraction ->
                            val seekTo = (fraction * durationMs).toLong()
                            exoPlayer?.seekTo(seekTo)
                            currentPositionMs = seekTo
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${FileSizeFormatter.formatDuration(currentPositionMs)} / ${FileSizeFormatter.formatDuration(durationMs)}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isFullscreenScrubber) "[HIDE SCRUB]" else "[SCRUB]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.accent,
                            modifier = Modifier.clickable {
                                isFullscreenScrubber = !isFullscreenScrubber
                            }
                        )
                        Text(
                            text = "${file.extension.uppercase()} • ${FileSizeFormatter.format(file.sizeBytes)}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
