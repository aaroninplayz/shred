package com.shred.app.ui.screens.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
    onDragDelta: (Float, Float) -> Unit = { _, _ -> },
    onFolderLongPress: (() -> Unit)? = null,
    onZoomModeChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val scope = rememberCoroutineScope()

    val offsetX = remember(file.id) { Animatable(0f) }
    val offsetY = remember(file.id) { Animatable(0f) }
    val flipProgress = remember(file.id) { Animatable(0f) } // 0f = front, 180f = back

    var isFlipped by remember(file.id) { mutableStateOf(false) }
    var isZoomMode by remember(file.id) { mutableStateOf(false) }

    LaunchedEffect(isZoomMode) {
        onZoomModeChanged?.invoke(isZoomMode)
    }

    val rotationZ = if (isFlipped) 0f else (offsetX.value / 60f).coerceIn(-25f, 25f)

    // Calculate color tint overlay based on drag offset with axis dominance check
    val dragX = offsetX.value
    val dragY = offsetY.value
    val absDragX = abs(dragX)
    val absDragY = abs(dragY)

    val tintColor = when {
        dragX < -60f && absDragX > absDragY * 1.2f -> SwipeDeleteRed.copy(alpha = (absDragX / 400f).coerceIn(0f, 0.5f))
        dragX > 60f && absDragX > absDragY * 1.2f -> SwipeKeepGreen.copy(alpha = (absDragX / 400f).coerceIn(0f, 0.5f))
        dragY < -60f && absDragY > absDragX * 1.2f -> SwipeFavoriteGold.copy(alpha = (absDragY / 400f).coerceIn(0f, 0.5f))
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                transformOrigin = TransformOrigin.Center // Pivot in-place at exact center
                this.rotationZ = rotationZ
                this.rotationX = flipProgress.value
                cameraDistance = 18f * density
                shape = androidx.compose.foundation.shape.RoundedCornerShape(colors.cardCornerRadius)
                clip = true
            }
            .border(
                width = if (isZoomMode) 0.dp else colors.borderWidth,
                color = if (isZoomMode) Color.Transparent else colors.border,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(colors.cardCornerRadius)
            )
            .background(colors.cardBackground, shape = androidx.compose.foundation.shape.RoundedCornerShape(colors.cardCornerRadius))
            .pointerInput(file.id, isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard) return@pointerInput

                detectTapGestures(
                    onLongPress = {
                        if (!isFlipped && (file.mediaType == MediaType.PHOTO || file.mediaType == MediaType.VIDEO)) {
                            isZoomMode = !isZoomMode
                        } else {
                            onFolderLongPress?.invoke()
                        }
                    }
                )
            }
            .pointerInput(file.id, isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard || isZoomMode) return@pointerInput

                detectDragGestures(
                    onDragEnd = {
                        val currentX = offsetX.value
                        val currentY = offsetY.value
                        val absX = abs(currentX)
                        val absY = abs(currentY)

                        val isHorizontalAction = absX > absY * 1.25f
                        val isVerticalAction = absY > absX * 1.25f

                        val thresholdX = 180f
                        val thresholdFavoriteY = 200f
                        val thresholdFlipY = 140f

                        scope.launch {
                            if (isFlipped) {
                                // In metadata view, swipe UP flips back
                                if ((isVerticalAction && currentY < -90f) || flipProgress.value < 90f) {
                                    launch { flipProgress.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) }
                                    isFlipped = false
                                } else {
                                    launch { flipProgress.animateTo(180f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) }
                                }
                                launch { offsetX.animateTo(0f, spring()) }
                                launch { offsetY.animateTo(0f, spring()) }
                            } else {
                                when {
                                    isHorizontalAction && currentX < -thresholdX -> {
                                        // SWIPE LEFT -> DELETE
                                        offsetX.animateTo(-1200f, tween(220))
                                        onSwiped(ActionType.DELETE)
                                    }
                                    isHorizontalAction && currentX > thresholdX -> {
                                        // SWIPE RIGHT -> KEEP
                                        offsetX.animateTo(1200f, tween(220))
                                        onSwiped(ActionType.KEEP)
                                    }
                                    isVerticalAction && currentY < -thresholdFavoriteY -> {
                                        // SWIPE UP -> FAVORITE
                                        offsetY.animateTo(-1400f, tween(220))
                                        onSwiped(ActionType.FAVORITE)
                                    }
                                    (isVerticalAction && currentY > thresholdFlipY) || flipProgress.value >= 90f -> {
                                        // SWIPE DOWN -> FLIP CARD TO METADATA
                                        launch { flipProgress.animateTo(180f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) }
                                        launch { offsetX.animateTo(0f, spring()) }
                                        launch { offsetY.animateTo(0f, spring()) }
                                        isFlipped = true
                                    }
                                    else -> {
                                        // Settle back to front
                                        launch { flipProgress.animateTo(0f, spring()) }
                                        launch { offsetX.animateTo(0f, spring()) }
                                        launch { offsetY.animateTo(0f, spring()) }
                                    }
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            launch { flipProgress.animateTo(if (isFlipped) 180f else 0f, spring()) }
                            launch { offsetX.animateTo(0f, spring()) }
                            launch { offsetY.animateTo(0f, spring()) }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            if (!isFlipped) {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                                onDragDelta(offsetX.value, offsetY.value)

                                // Continuous 3D Flip Rotation tracking vertical drag down distance with axis dominance check
                                val isVerticalDrag = abs(offsetY.value) > abs(offsetX.value) * 1.3f
                                if (offsetY.value > 20f && isVerticalDrag) {
                                    val continuousFlip = (offsetY.value / 160f * 180f).coerceIn(0f, 180f)
                                    flipProgress.snapTo(continuousFlip)
                                } else {
                                    flipProgress.snapTo(0f)
                                }
                            } else {
                                offsetY.snapTo(offsetY.value + dragAmount.y)

                                // Continuous 3D Flip Rotation tracking drag up distance back to 0
                                if (offsetY.value < 0f) {
                                    val continuousFlip = (180f + offsetY.value / 160f * 180f).coerceIn(0f, 180f)
                                    flipProgress.snapTo(continuousFlip)
                                } else {
                                    flipProgress.snapTo(180f)
                                }
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
