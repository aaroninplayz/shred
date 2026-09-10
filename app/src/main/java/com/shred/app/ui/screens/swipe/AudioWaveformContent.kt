package com.shred.app.ui.screens.swipe

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.shred.app.data.model.MediaFile
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter
import com.shred.app.util.WaveformSampler
import kotlinx.coroutines.delay
import java.io.File

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

    LaunchedEffect(file.path) {
        waveformBars = WaveformSampler.sampleWaveform(file.path, barCount = 36)
    }

    val exoPlayer = remember(file.path) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.fromFile(File(file.path)))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition
            durationMs = exoPlayer.duration.coerceAtLeast(1L)
            delay(150)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cardBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "AUDIO TRACK ///",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = file.fileName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.textPrimary,
                maxLines = 2
            )
        }

        // Center Waveform + Play button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Waveform Amplitude Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val progressFraction = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()) else 0f
                waveformBars.forEachIndexed { index, amplitude ->
                    val barFraction = index.toFloat() / waveformBars.size
                    val isPlayed = barFraction <= progressFraction
                    val barHeight = (amplitude * 80).coerceIn(8f, 80f).dp

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .background(if (isPlayed) colors.accent else colors.borderSubtle)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Play / Pause Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(2.5.dp, colors.border)
                    .background(colors.surface)
                    .clickable {
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                            isPlaying = false
                        } else {
                            exoPlayer.play()
                            isPlaying = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = colors.accent,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom Timing Info
        Row(
            modifier = Modifier.fillMaxWidth(),
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
}
