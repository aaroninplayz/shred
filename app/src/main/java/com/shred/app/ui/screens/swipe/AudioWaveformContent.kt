package com.shred.app.ui.screens.swipe

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.shred.app.data.model.MediaFile
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter
import com.shred.app.util.WaveformSampler
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AudioWaveformContent(
    file: MediaFile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalShredColors.current

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var waveformBars by remember { mutableStateOf<List<Float>>(emptyList()) }
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(file.path) {
        waveformBars = try {
            WaveformSampler.sampleWaveform(file.path, barCount = 42)
        } catch (e: Exception) {
            Log.e("AudioWaveformContent", "Waveform sampling failed for ${file.fileName}", e)
            List(42) { kotlin.random.Random.nextFloat().coerceIn(0.15f, 1.0f) }
        }
    }

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
                            Log.e("AudioWaveformContent", "ExoPlayer error for ${file.fileName}: ${error.message}", error)
                            isPlaying = false
                            hasError = true
                        }
                    })
                    // NOTE: prepare() is called lazily on first play tap (not here)
                    playWhenReady = false
                }
            }
        } catch (e: Exception) {
            Log.e("AudioWaveformContent", "ExoPlayer creation failed for ${file.fileName}", e)
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
                Log.w("AudioWaveformContent", "Error releasing ExoPlayer", e)
            }
        }
    }

    // Update position and live visualizer animation ticker while playing
    LaunchedEffect(isPlaying) {
        val player = exoPlayer ?: return@LaunchedEffect
        while (isPlaying && isActive && !hasError) {
            try {
                currentPositionMs = player.currentPosition
                durationMs = player.duration.coerceAtLeast(1L)
                animTime += 0.05f
            } catch (e: Exception) {
                Log.e("AudioWaveformContent", "Polling error", e)
                isPlaying = false
                hasError = true
                break
            }
            delay(50)
        }
    }

    val togglePlayPause = {
        val player = exoPlayer
        if (player != null && !hasError) {
            if (player.isPlaying) {
                try {
                    player.pause()
                } catch (e: Exception) {
                    Log.e("AudioWaveformContent", "Pause error", e)
                    hasError = true
                }
                isPlaying = false
            } else {
                try {
                    if (player.playbackState == androidx.media3.common.Player.STATE_IDLE) {
                        player.prepare()
                    }
                    player.play()
                    isPlaying = true
                } catch (e: Exception) {
                    Log.e("AudioWaveformContent", "Play error", e)
                    isPlaying = false
                    hasError = true
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cardBackground)
            .clickable { togglePlayPause() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Audio Track Info Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "[ 🎧 AUDIO TRACK ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = file.fileName,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Center Live Equalizer Visualizer & Tap-Status Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Frequency Spectrum Waveform Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progressFraction = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

                    waveformBars.forEachIndexed { index, baseAmp ->
                        val barFraction = index.toFloat() / waveformBars.size.coerceAtLeast(1)
                        val isPlayed = barFraction <= progressFraction

                        // Live audio frequency height modulation when playing
                        val liveHeight = if (isPlaying) {
                            (baseAmp * 60f + sin(animTime * 6f + index * 0.45f) * 22f + cos(animTime * 10f + index * 0.7f) * 14f).coerceIn(6f, 100f)
                        } else {
                            (baseAmp * 75f).coerceIn(6f, 90f)
                        }

                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(liveHeight.dp)
                                .background(if (isPlayed) colors.accent else colors.borderSubtle)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Play / Pause Tap Indicator Badge
                Box(
                    modifier = Modifier
                        .border(2.dp, if (isPlaying) colors.accent else colors.border)
                        .background(if (isPlaying) colors.accent.copy(alpha = 0.15f) else colors.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isPlaying) "[ ▶ PLAYING • TAP TO PAUSE ]" else "[ ⏸ PAUSED • TAP TO PLAY ]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = if (isPlaying) colors.accent else colors.textPrimary
                    )
                }
            }

            // Bottom Timing & Format Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${FileSizeFormatter.formatDuration(currentPositionMs)} / ${FileSizeFormatter.formatDuration(durationMs)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.textPrimary
                )

                Text(
                    text = "${file.extension.uppercase()} • ${FileSizeFormatter.format(file.sizeBytes)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Stealth Bottom Scrubber Progress Line (Full-width 4.dp bottom edge line with drag & tap seeking)
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(28.dp) // Touch gesture target area
                .pointerInput(exoPlayer, durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0 && exoPlayer != null) {
                            val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            val seekTo = (fraction * durationMs).toLong()
                            try {
                                exoPlayer.seekTo(seekTo)
                                currentPositionMs = seekTo
                            } catch (e: Exception) {}
                        }
                    }
                }
                .pointerInput(exoPlayer, durationMs) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (durationMs > 0 && exoPlayer != null) {
                            val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            val seekTo = (fraction * durationMs).toLong()
                            try {
                                exoPlayer.seekTo(seekTo)
                                currentPositionMs = seekTo
                            } catch (e: Exception) {}
                        }
                    }
                },
            contentAlignment = Alignment.BottomStart
        ) {
            val progressFraction = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

            // Bottom Track Base Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(colors.borderSubtle)
            )

            // Bottom Track Active Progress Line
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .height(4.dp)
                    .background(colors.accent)
            )
        }
    }
}
