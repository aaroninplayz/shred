package com.shred.app.ui.screens.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredColors

import androidx.compose.runtime.key
import java.io.File

@Composable
fun CardStack(
    files: List<MediaFile>,
    onSwiped: (ActionType, MediaFile) -> Unit,
    onFolderLongPress: (() -> Unit)? = null,
    onZoomModeChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Take up to 4 items for the stack
        val visibleFiles = files.take(4).reversed()

        visibleFiles.forEachIndexed { reversedIndex, file ->
            val indexFromTop = visibleFiles.size - 1 - reversedIndex
            val isTopCard = indexFromTop == 0

            // Progressive depth, scale & blur for brutalist card stack
            val scale = 1f - (indexFromTop * 0.04f)
            val offsetY = (indexFromTop * 14).dp
            val offsetX = (indexFromTop * 6).dp
            val rotationZ = if (indexFromTop % 2 == 1) 2.5f else -1.5f * indexFromTop
            val alpha = when (indexFromTop) {
                0 -> 1.0f
                1 -> 0.85f
                2 -> 0.65f
                else -> 0.45f
            }
            val blurRadius = (indexFromTop * 6).dp

            key(file.id) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.86f)
                        .aspectRatio(0.72f)
                        .offset(x = offsetX, y = offsetY)
                        .scale(scale)
                        .graphicsLayer {
                            this.rotationZ = if (isTopCard) 0f else rotationZ
                        }
                        .alpha(alpha)
                        .then(
                            if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier
                        )
                ) {
                    if (isTopCard) {
                        // Top card: full interactive MediaCard with ExoPlayer/zoom/gestures
                        MediaCard(
                            file = file,
                            isTopCard = true,
                            onSwiped = { action -> onSwiped(action, file) },
                            onDragDelta = { _, _ -> },
                            onFolderLongPress = onFolderLongPress,
                            onZoomModeChanged = { isZoomed ->
                                onZoomModeChanged?.invoke(isZoomed)
                            }
                        )
                    } else {
                        // Background cards: lightweight thumbnail placeholder
                        // No ExoPlayer, no zoom, no gesture handling — just a visual preview
                        BackgroundCardThumbnail(
                            file = file,
                            context = context
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lightweight thumbnail for background (non-top) cards in the stack.
 * Renders a small image thumbnail for photos/videos, or a styled placeholder for audio.
 * Does NOT create ExoPlayer instances or interactive gesture handlers.
 */
@Composable
private fun BackgroundCardThumbnail(
    file: MediaFile,
    context: android.content.Context
) {
    val colors = LocalShredColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.cardBackground)
            .border(
                width = colors.borderWidth,
                color = colors.border,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(colors.cardCornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (file.mediaType) {
            MediaType.PHOTO -> {
                // Small thumbnail for photo — uses tiny decode size
                val diskFile = remember(file.path) { File(file.path) }
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (diskFile.exists()) diskFile else file.path)
                        .size(200, 300)  // Very small thumbnail for background card
                        .crossfade(false)
                        .memoryCacheKey("thumb_${file.path}")
                        .diskCacheKey("thumb_${file.path}")
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            MediaType.VIDEO -> {
                // Video frame thumbnail — uses Coil's VideoFrameDecoder
                val diskFile = remember(file.path) { File(file.path) }
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (diskFile.exists()) diskFile else file.path)
                            .size(200, 300)
                            .crossfade(false)
                            .memoryCacheKey("thumb_${file.path}")
                            .diskCacheKey("thumb_${file.path}")
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Video indicator overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            MediaType.AUDIO -> {
                // Audio placeholder — styled box with icon, no ExoPlayer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎧",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = file.fileName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
