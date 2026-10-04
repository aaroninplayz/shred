package com.shred.app.ui.screens.favorites

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.ui.screens.swipe.AudioWaveformContent
import com.shred.app.ui.screens.swipe.MetadataBackView
import com.shred.app.ui.screens.swipe.PhotoCardContent
import com.shred.app.ui.screens.swipe.VideoCardContent
import com.shred.app.ui.theme.LocalShredAnimation
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeFavoriteGold
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FavoriteMediaCard(
    file: MediaFile,
    isTopCard: Boolean,
    onLoop: () -> Unit,
    onUnfavorite: () -> Unit,
    onZoomModeChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val animSpec = LocalShredAnimation.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val offsetX = remember(file.id) { Animatable(0f) }
    val offsetY = remember(file.id) { Animatable(0f) }
    val flipProgress = remember(file.id) { Animatable(0f) }

    var verticalDragUp by remember(file.id) { mutableFloatStateOf(0f) }
    var verticalDragDown by remember(file.id) { mutableFloatStateOf(0f) }

    var isFlipped by remember(file.id) { mutableStateOf(false) }
    var isZoomMode by remember(file.id) { mutableStateOf(false) }

    LaunchedEffect(isZoomMode) {
        onZoomModeChanged?.invoke(isZoomMode)
    }

    val rotationZ = if (isFlipped) 0f else (offsetX.value / 60f).coerceIn(-20f, 20f)

    val maxDragDown = with(density) { 150.dp.toPx() }
    val commitThresholdDown = with(density) { 85.dp.toPx() }

    // Unfavorite progressive mask (Up drag)
    val unfavoriteAlpha = if (verticalDragUp > 30f && !isFlipped) {
        (verticalDragUp / 320f).coerceIn(0f, 0.70f)
    } else 0f

    // Metadata flip progressive grayscale (Down drag)
    val grayscaleProgress = if (!isFlipped && verticalDragDown > 10f) {
        (verticalDragDown / maxDragDown).coerceIn(0f, 0.5f) + (flipProgress.value / 180f * 0.5f)
    } else if (isFlipped) {
        1.0f
    } else {
        0.0f
    }

    val cardShape = RoundedCornerShape(colors.cardCornerRadius)
    val flipAngle = flipProgress.value
    val sinAngle = abs(kotlin.math.sin(Math.toRadians(flipAngle.toDouble()))).toFloat()

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .then(
                if (colors.isGlass) {
                    Modifier
                        .shadow(
                            elevation = if (isZoomMode) 0.dp else 12.dp,
                            shape = cardShape,
                            ambientColor = Color(0x3300F2FE),
                            spotColor = Color(0x22FF007F)
                        )
                        .border(
                            width = if (isZoomMode) 0.dp else 1.dp,
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(colors.border, colors.borderSubtle)),
                            shape = cardShape
                        )
                } else if (colors.shadowElevation > 0.dp) {
                    Modifier
                        .shadow(colors.shadowElevation, cardShape)
                        .border(
                            width = if (isZoomMode) 0.dp else colors.borderWidth,
                            color = if (isZoomMode) Color.Transparent else colors.border,
                            shape = cardShape
                        )
                } else {
                    Modifier.border(
                        width = if (isZoomMode) 0.dp else colors.borderWidth,
                        color = if (isZoomMode) Color.Transparent else colors.border,
                        shape = cardShape
                    )
                }
            )
            .graphicsLayer {
                transformOrigin = TransformOrigin.Center
                this.rotationZ = rotationZ
                this.rotationX = flipAngle
                cameraDistance = 18f * density.density
            }
            .background(colors.cardBackground, shape = cardShape)
            .pointerInput(file.id, isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard) return@pointerInput

                detectTapGestures(
                    onLongPress = {
                        if (!isFlipped && (file.mediaType == MediaType.PHOTO || file.mediaType == MediaType.VIDEO)) {
                            isZoomMode = !isZoomMode
                        }
                    }
                )
            }
            .pointerInput(file.id, isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard || isZoomMode) return@pointerInput

                detectDragGestures(
                    onDragStart = {
                        verticalDragUp = 0f
                        verticalDragDown = 0f
                    },
                    onDragEnd = {
                        val currentX = offsetX.value
                        val absX = abs(currentX)

                        val thresholdX = 160f
                        val thresholdUnfavoriteY = 200f
                        val thresholdFlipY = 140f

                        scope.launch {
                            if (isFlipped) {
                                if (verticalDragUp > 90f || flipProgress.value < 90f) {
                                    launch { flipProgress.animateTo(0f, animSpec.flipSpring) }
                                    isFlipped = false
                                } else {
                                    launch { flipProgress.animateTo(180f, animSpec.flipSpring) }
                                }
                                launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                            } else {
                                when {
                                    // Horizontal Swipes (Left or Right) -> LOOP card to bottom of deck
                                    absX > verticalDragUp && absX > verticalDragDown && absX > thresholdX -> {
                                        val exitX = if (currentX > 0) 1400f else -1400f
                                        offsetX.animateTo(exitX, tween(animSpec.swipeFlingDurationMs))
                                        onLoop()
                                    }
                                    // Upward Swipe -> UNFAVORITE (remove from vault)
                                    verticalDragUp >= thresholdUnfavoriteY -> {
                                        offsetY.animateTo(-1500f, tween(animSpec.swipeFlingDurationMs))
                                        onUnfavorite()
                                    }
                                    // Downward Swipe -> 3D FLIP to metadata
                                    offsetY.value >= commitThresholdDown -> {
                                        launch { offsetY.animateTo(0f, tween(260)) }
                                        launch { flipProgress.animateTo(180f, tween(320)) }
                                        launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                        isFlipped = true
                                    }
                                    else -> {
                                        launch { flipProgress.animateTo(0f, animSpec.flipSpring) }
                                        launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                        launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                    }
                                }
                            }
                            verticalDragUp = 0f
                            verticalDragDown = 0f
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            launch { flipProgress.animateTo(if (isFlipped) 180f else 0f, animSpec.flipSpring) }
                            launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                            launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                            verticalDragUp = 0f
                            verticalDragDown = 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            if (!isFlipped) {
                                val absDeltaX = abs(dragAmount.x)
                                val absDeltaY = abs(dragAmount.y)

                                if (dragAmount.y < 0 && (abs(offsetY.value) > abs(offsetX.value) || absDeltaY > absDeltaX)) {
                                    // Dragging UP -> UNFAVORITE (stationary card)
                                    verticalDragUp += abs(dragAmount.y)
                                    verticalDragDown = (verticalDragDown - abs(dragAmount.y)).coerceAtLeast(0f)
                                } else if (dragAmount.y > 0 && (abs(offsetY.value) > abs(offsetX.value) || absDeltaY > absDeltaX)) {
                                    // Dragging DOWN -> Physical downward translation
                                    val nextY = (offsetY.value + dragAmount.y).coerceIn(0f, maxDragDown)
                                    offsetY.snapTo(nextY)
                                    verticalDragDown = nextY
                                    verticalDragUp = 0f
                                    offsetX.snapTo(0f)
                                } else {
                                    // Horizontal drag -> follows finger 1:1 for looping
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                }
                            } else {
                                if (dragAmount.y < 0) {
                                    verticalDragUp += abs(dragAmount.y)
                                    val continuousFlip = (180f - (verticalDragUp / 140f * 90f)).coerceIn(90f, 180f)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationX = 180f }
            ) {
                MetadataBackView(
                    file = file,
                    onFlipBack = {
                        scope.launch {
                            flipProgress.animateTo(0f, animSpec.flipSpring)
                            isFlipped = false
                        }
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        if (grayscaleProgress > 0.01f) {
                            drawRect(
                                color = Color.Gray.copy(alpha = grayscaleProgress),
                                blendMode = BlendMode.Color
                            )
                        }
                    }
            ) {
                when (file.mediaType) {
                    MediaType.PHOTO -> PhotoCardContent(file = file, isZoomMode = isZoomMode)
                    MediaType.VIDEO -> VideoCardContent(file = file, isZoomMode = isZoomMode)
                    MediaType.AUDIO -> AudioWaveformContent(file = file)
                }

                // Progressive Unfavorite Mask (Yellow)
                if (unfavoriteAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SwipeFavoriteGold.copy(alpha = unfavoriteAlpha))
                    )
                }
            }
        }

        // 3D Card Thickness Rim Slab: visible during 3D rotation so card never collapses to 0 height
        if (sinAngle > 0.02f) {
            val slabThickness = 14.dp * sinAngle
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(slabThickness)
                    .background(colors.surface.copy(alpha = 0.95f))
                    .border(1.dp, colors.border.copy(alpha = 0.8f))
            )
        }
    }
}
