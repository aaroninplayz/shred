package com.shred.app.ui.screens.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeDeleteRed
import com.shred.app.ui.theme.SwipeFavoriteGold
import com.shred.app.ui.theme.SwipeKeepGreen
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MediaCard(
    file: MediaFile,
    isTopCard: Boolean,
    onSwiped: (ActionType) -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val flipProgress = remember { Animatable(0f) } // 0f = front, 180f = back

    var isFlipped by remember { mutableStateOf(false) }
    var isZoomMode by remember { mutableStateOf(false) }

    val rotationZ = if (isFlipped) 0f else (offsetX.value / 60f).coerceIn(-25f, 25f)

    // Calculate color tint overlay based on drag offset
    val dragX = offsetX.value
    val dragY = offsetY.value

    val tintColor = when {
        dragX < -60f -> SwipeDeleteRed.copy(alpha = (abs(dragX) / 400f).coerceIn(0f, 0.5f))
        dragX > 60f -> SwipeKeepGreen.copy(alpha = (abs(dragX) / 400f).coerceIn(0f, 0.5f))
        dragY < -60f -> SwipeFavoriteGold.copy(alpha = (abs(dragY) / 400f).coerceIn(0f, 0.5f))
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 1.0f) // Pivot at bottom-center
                this.rotationZ = rotationZ
                this.rotationX = flipProgress.value
                cameraDistance = 16f * density
            }
            .border(3.dp, colors.border)
            .pointerInput(isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard) return@pointerInput

                detectTapGestures(
                    onLongPress = {
                        // Long press toggles zoom mode
                        if (!isFlipped && (file.mediaType == MediaType.PHOTO || file.mediaType == MediaType.VIDEO)) {
                            isZoomMode = !isZoomMode
                        }
                    }
                )
            }
            .pointerInput(isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard || isZoomMode) return@pointerInput

                detectDragGestures(
                    onDragEnd = {
                        val currentX = offsetX.value
                        val currentY = offsetY.value
                        val thresholdX = 220f
                        val thresholdY = 240f

                        scope.launch {
                            if (isFlipped) {
                                // In metadata view, swipe UP flips back
                                if (currentY < -100f) {
                                    flipProgress.animateTo(0f, tween(300))
                                    isFlipped = false
                                }
                                offsetX.animateTo(0f, tween(200))
                                offsetY.animateTo(0f, tween(200))
                            } else {
                                when {
                                    currentX < -thresholdX -> {
                                        // SWIPE LEFT -> DELETE
                                        offsetX.animateTo(-1200f, tween(220))
                                        onSwiped(ActionType.DELETE)
                                    }
                                    currentX > thresholdX -> {
                                        // SWIPE RIGHT -> KEEP
                                        offsetX.animateTo(1200f, tween(220))
                                        onSwiped(ActionType.KEEP)
                                    }
                                    currentY < -thresholdY -> {
                                        // SWIPE UP -> FAVORITE
                                        offsetY.animateTo(-1400f, tween(220))
                                        onSwiped(ActionType.FAVORITE)
                                    }
                                    currentY > 120f -> {
                                        // SWIPE DOWN -> FLIP CARD TO METADATA
                                        offsetY.animateTo(0f, tween(150))
                                        offsetX.animateTo(0f, tween(150))
                                        flipProgress.animateTo(180f, tween(350))
                                        isFlipped = true
                                    }
                                    else -> {
                                        // Settle back to center
                                        offsetX.animateTo(0f, tween(200))
                                        offsetY.animateTo(0f, tween(200))
                                    }
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
                            if (!isFlipped) {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                                onDragDelta(offsetX.value, offsetY.value)
                            } else {
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                            }
                        }
                    }
                )
            }
    ) {
        if (flipProgress.value >= 90f) {
            // Back of card (Metadata) - upside-down flip compensation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationX = 180f }
            ) {
                MetadataBackView(file = file)
            }
        } else {
            // Front of card (Media Content)
            Box(modifier = Modifier.fillMaxSize()) {
                when (file.mediaType) {
                    MediaType.PHOTO -> PhotoCardContent(file = file, isZoomMode = isZoomMode)
                    MediaType.VIDEO -> VideoCardContent(file = file, isZoomMode = isZoomMode)
                    MediaType.AUDIO -> AudioWaveformContent(file = file)
                }

                // Dynamic Action Color Tint Overlay
                if (tintColor != Color.Transparent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tintColor)
                    )
                }
            }
        }
    }
}
