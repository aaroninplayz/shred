package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.shred.app.data.model.MediaFile
import com.shred.app.ui.components.rememberShimmerBrush
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.util.FileSizeFormatter
import java.io.File

@Composable
fun PhotoCardContent(
    file: MediaFile,
    isZoomMode: Boolean,
    onToggleZoom: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalShredColors.current
    val shimmerBrush = rememberShimmerBrush()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        ZoomableBox(
            isZoomMode = isZoomMode,
            onToggleZoom = onToggleZoom,
            modifier = Modifier.fillMaxSize()
        ) {
            val diskFile = remember(file.path) { File(file.path) }
            val fileExists = remember(file.path) { diskFile.exists() && diskFile.length() > 0 }

            if (!fileExists) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.cardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[ FILE NOT FOUND / MOVED ]\n${file.fileName}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.accent
                    )
                }
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(diskFile)
                        .size(1080, 1620)
                        .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                        .memoryCacheKey(file.path)
                        .diskCacheKey(file.path)
                        .crossfade(100)
                        .build(),
                    contentDescription = file.fileName,
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(shimmerBrush)
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.cardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "[ FILE UNREADABLE / CORRUPTED ]\n${file.fileName}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = colors.accent
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Floating Zoom Mode Header Pill
        if (isZoomMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(9999.dp))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🔍 DOUBLE-TAP TO EXIT ZOOM",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF00E5FF)
                )
            }
        }

        // Bottom right metadata pill (only when not zoomed)
        if (!isZoomMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
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
