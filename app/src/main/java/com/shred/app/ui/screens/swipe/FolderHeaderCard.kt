package com.shred.app.ui.screens.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.FolderGroup
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeDeleteRed
import com.shred.app.ui.theme.SwipeFavoriteGold
import com.shred.app.ui.theme.SwipeKeepGreen
import com.shred.app.util.FileSizeFormatter
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FolderHeaderCard(
    folderGroup: FolderGroup,
    onSwiped: (ActionType) -> Unit,
    onReshuffle: () -> Unit,
    onExpandClick: () -> Unit,
    onFolderLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val offsetX = remember(folderGroup.folderPath) { Animatable(0f) }
    val offsetY = remember(folderGroup.folderPath) { Animatable(0f) }

    val rotationZ = (offsetX.value / 60f).coerceIn(-25f, 25f)
    val dragX = offsetX.value
    val dragY = offsetY.value

    val tintColor = when {
        dragX < -60f -> SwipeDeleteRed.copy(alpha = (abs(dragX) / 400f).coerceIn(0f, 0.5f))
        dragX > 60f -> SwipeKeepGreen.copy(alpha = (abs(dragX) / 400f).coerceIn(0f, 0.5f))
        dragY < -60f -> SwipeFavoriteGold.copy(alpha = (abs(dragY) / 400f).coerceIn(0f, 0.5f))
        else -> Color.Transparent
    }

    val sampleFiles = remember(folderGroup.folderPath) { folderGroup.files.take(4) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                transformOrigin = TransformOrigin.Center
                this.rotationZ = rotationZ
            }
            .border(3.dp, colors.border)
            .pointerInput(folderGroup.folderPath) {
                detectTapGestures(
                    onTap = { onExpandClick() },
                    onLongPress = { onFolderLongPress?.invoke() }
                )
            }
            .pointerInput(folderGroup.folderPath) {
                detectDragGestures(
                    onDragEnd = {
                        val currentX = offsetX.value
                        val currentY = offsetY.value
                        val absX = abs(currentX)
                        val absY = abs(currentY)

                        val isHorizontalAction = absX > absY * 1.25f
                        val isVerticalAction = absY > absX * 1.25f

                        val thresholdX = 180f
                        val thresholdY = 200f

                        scope.launch {
                            when {
                                isHorizontalAction && currentX < -thresholdX -> {
                                    offsetX.animateTo(-1200f, tween(220))
                                    onSwiped(ActionType.DELETE)
                                }
                                isHorizontalAction && currentX > thresholdX -> {
                                    offsetX.animateTo(1200f, tween(220))
                                    onSwiped(ActionType.KEEP)
                                }
                                isVerticalAction && currentY < -thresholdY -> {
                                    offsetY.animateTo(-1400f, tween(220))
                                    onSwiped(ActionType.FAVORITE)
                                }
                                isVerticalAction && currentY > thresholdY -> {
                                    // Swipe down reshuffles folder card to back of queue
                                    offsetY.animateTo(1200f, tween(220))
                                    onReshuffle()
                                }
                                else -> {
                                    offsetX.animateTo(0f, tween(200))
                                    offsetY.animateTo(0f, tween(200))
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, tween(200))
                            offsetY.animateTo(0f, tween(200))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                )
            }
    ) {
        // Heavily Blurred 2x2 Photo Collage Background
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .graphicsLayer { alpha = 0.5f }
            ) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    sampleFiles.getOrNull(0)?.let { file ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(File(file.path)).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    sampleFiles.getOrNull(1)?.let { file ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(File(file.path)).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    sampleFiles.getOrNull(2)?.let { file ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(File(file.path)).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    sampleFiles.getOrNull(3)?.let { file ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(File(file.path)).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }

            // Dark semi-transparent scrim over image collage for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }

        // Folder Card Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header Section: FOLDER, Folder Name, Address
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "FOLDER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = folderGroup.folderName,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = Color.White,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = folderGroup.folderPath,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            // Center Stats Section: Number of files and total size
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, colors.borderSubtle)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${folderGroup.fileCount} FILES",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "TOTAL SIZE: ${FileSizeFormatter.format(folderGroup.totalSizeBytes)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            // Bottom Hint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSubtle)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TAP TO UNPACK /// SWIPE TO ACTION",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Action Color Tint Overlay during drag
        if (tintColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tintColor)
            )
        }
    }
}

