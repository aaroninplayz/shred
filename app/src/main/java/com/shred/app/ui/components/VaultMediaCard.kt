package com.shred.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Text
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
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class VaultGestureLock {
    NONE,
    HORIZONTAL,
    VERTICAL_UP,
    VERTICAL_DOWN
}

@Composable
fun VaultMediaCard(
    file: MediaFile,
    isTopCard: Boolean,
    actionColor: Color,
    onLoop: () -> Unit,
    onActionUp: () -> Unit,
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
    val velocityTracker = remember(file.id) { VelocityTracker() }

    var gestureLock by remember(file.id) { mutableStateOf(VaultGestureLock.NONE) }
    var accumulatedX by remember(file.id) { mutableFloatStateOf(0f) }
    var accumulatedY by remember(file.id) { mutableFloatStateOf(0f) }
    var verticalDragUp by remember(file.id) { mutableFloatStateOf(0f) }
    var verticalDragDown by remember(file.id) { mutableFloatStateOf(0f) }

    var isFlipped by remember(file.id) { mutableStateOf(false) }
    var isZoomMode by remember(file.id) { mutableStateOf(false) }

    LaunchedEffect(isZoomMode) {
        onZoomModeChanged?.invoke(isZoomMode)
    }

    val rotationZ = if (isFlipped) 0f else (offsetX.value / 40f).coerceIn(-24f, 24f)

    val tolerancePx = with(density) { 24.dp.toPx() }
    val maxDragX = with(density) { 220.dp.toPx() }
    val maxDragUp = with(density) { 220.dp.toPx() }
    val maxDragDown = with(density) { 150.dp.toPx() }
    val commitThresholdX = with(density) { 130.dp.toPx() }
    val commitThresholdUp = with(density) { 130.dp.toPx() }
    val commitThresholdDown = with(density) { 85.dp.toPx() }

    // Progressive color mask for action up (NO TEXT BANNERS)
    val actionAlpha = if (gestureLock == VaultGestureLock.VERTICAL_UP && !isFlipped) {
        (abs(offsetY.value) / maxDragUp * 0.45f).coerceIn(0f, 0.45f)
    } else 0f

    // Progressive cyan mask for downward drag (METADATA inspection)
    val downMaskAlpha = if (gestureLock == VaultGestureLock.VERTICAL_DOWN && !isFlipped) {
        (verticalDragDown / maxDragDown * 0.45f).coerceIn(0f, 0.45f)
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
                            brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle)),
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
                cameraDistance = 20f * density.density
            }
            .background(colors.cardBackground, shape = cardShape)
            .pointerInput(file.id, isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard) return@pointerInput

                detectTapGestures(
                    onDoubleTap = {
                        if (!isFlipped && file.mediaType == MediaType.PHOTO) {
                            isZoomMode = !isZoomMode
                        }
                    },
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
                    onDragStart = { startOffset ->
                        velocityTracker.resetTracking()
                        velocityTracker.addPosition(System.currentTimeMillis(), startOffset)
                        gestureLock = VaultGestureLock.NONE
                        accumulatedX = 0f
                        accumulatedY = 0f
                        verticalDragUp = 0f
                        verticalDragDown = 0f
                    },
                    onDragEnd = {
                        scope.launch {
                            if (isFlipped) {
                                if (verticalDragUp > with(density) { 60.dp.toPx() } || flipProgress.value < 90f) {
                                    launch { flipProgress.animateTo(0f, animSpec.flipSpring) }
                                    isFlipped = false
                                } else {
                                    launch { flipProgress.animateTo(180f, animSpec.flipSpring) }
                                }
                                launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                verticalDragUp = 0f
                                verticalDragDown = 0f
                                gestureLock = VaultGestureLock.NONE
                            } else {
                                val velocity = velocityTracker.calculateVelocity()
                                val vx = velocity.x
                                val vy = velocity.y
                                val flingThresholdPx = with(density) { 950.dp.toPx() }
                                val minFlingDistX = with(density) { 45.dp.toPx() }
                                val minFlingDistUp = with(density) { 45.dp.toPx() }
                                val minFlingDistDown = with(density) { 35.dp.toPx() }

                                when (gestureLock) {
                                    VaultGestureLock.HORIZONTAL -> {
                                        val currentX = offsetX.value
                                        val isFlingHorizontal = abs(vx) > flingThresholdPx && abs(currentX) >= minFlingDistX
                                        if (abs(currentX) > commitThresholdX || isFlingHorizontal) {
                                            val exitX = if (currentX > 0 || vx > 0) 2200f else -2200f
                                            offsetX.animateTo(exitX, tween(140))
                                            onLoop()
                                        } else {
                                            launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                        }
                                    }
                                    VaultGestureLock.VERTICAL_UP -> {
                                        val isFlingUp = vy < -flingThresholdPx && abs(offsetY.value) >= minFlingDistUp
                                        if (abs(offsetY.value) > commitThresholdUp || isFlingUp) {
                                            offsetY.animateTo(-2400f, tween(140))
                                            onActionUp()
                                        } else {
                                            launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                        }
                                    }
                                    VaultGestureLock.VERTICAL_DOWN -> {
                                        val isFlingDown = vy > (flingThresholdPx * 0.85f) && offsetY.value >= minFlingDistDown
                                        if (offsetY.value >= commitThresholdDown || isFlingDown) {
                                            launch { offsetY.animateTo(0f, tween(260)) }
                                            launch { flipProgress.animateTo(180f, tween(320)) }
                                            isFlipped = true
                                        } else {
                                            launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                            launch { flipProgress.animateTo(0f, animSpec.flipSpring) }
                                        }
                                    }
                                    VaultGestureLock.NONE -> {
                                        launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                        launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                    }
                                }
                                verticalDragUp = 0f
                                verticalDragDown = 0f
                                gestureLock = VaultGestureLock.NONE
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            launch { flipProgress.animateTo(if (isFlipped) 180f else 0f, animSpec.flipSpring) }
                            launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                            launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                            verticalDragUp = 0f
                            verticalDragDown = 0f
                            gestureLock = VaultGestureLock.NONE
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch {
                            if (!isFlipped) {
                                if (gestureLock == VaultGestureLock.NONE) {
                                    accumulatedX += dragAmount.x
                                    accumulatedY += dragAmount.y
                                    val distSq = accumulatedX * accumulatedX + accumulatedY * accumulatedY
                                    if (distSq >= tolerancePx * tolerancePx) {
                                        if (abs(accumulatedX) >= abs(accumulatedY)) {
                                            gestureLock = VaultGestureLock.HORIZONTAL
                                            offsetY.snapTo(0f)
                                        } else if (accumulatedY < 0f) {
                                            gestureLock = VaultGestureLock.VERTICAL_UP
                                            offsetX.snapTo(0f)
                                        } else {
                                            gestureLock = VaultGestureLock.VERTICAL_DOWN
                                            offsetX.snapTo(0f)
                                        }
                                    } else {
                                        offsetX.snapTo(accumulatedX * 0.35f)
                                        offsetY.snapTo(accumulatedY * 0.35f)
                                    }
                                }

                                when (gestureLock) {
                                    VaultGestureLock.HORIZONTAL -> {
                                        val nextX = (offsetX.value + dragAmount.x).coerceIn(-maxDragX, maxDragX)
                                        offsetX.snapTo(nextX)
                                        offsetY.snapTo(0f)
                                    }
                                    VaultGestureLock.VERTICAL_UP -> {
                                        val nextY = (offsetY.value + dragAmount.y).coerceIn(-maxDragUp, 0f)
                                        offsetY.snapTo(nextY)
                                        verticalDragUp = abs(nextY)
                                        offsetX.snapTo(0f)
                                    }
                                    VaultGestureLock.VERTICAL_DOWN -> {
                                        val nextY = (offsetY.value + dragAmount.y).coerceIn(0f, maxDragDown)
                                        offsetY.snapTo(nextY)
                                        verticalDragDown = nextY
                                        offsetX.snapTo(0f)
                                    }
                                    VaultGestureLock.NONE -> {}
                                }
                            } else {
                                if (dragAmount.y < 0) {
                                    verticalDragUp += abs(dragAmount.y)
                                    val continuousFlip = (180f - (verticalDragUp / with(density) { 140.dp.toPx() } * 180f)).coerceIn(0f, 180f)
                                    flipProgress.snapTo(continuousFlip)
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
                    MediaType.PHOTO -> PhotoCardContent(
                        file = file,
                        isZoomMode = isZoomMode,
                        onToggleZoom = { isZoomMode = !isZoomMode }
                    )
                    MediaType.VIDEO -> VideoCardContent(file = file, isZoomMode = isZoomMode)
                    MediaType.AUDIO -> AudioWaveformContent(file = file)
                }

                // Progressive Action Mask (Up action in vault)
                if (actionAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(actionColor.copy(alpha = actionAlpha))
                    )
                }

                // Progressive Cyan Mask for downward drag (METADATA inspection)
                if (downMaskAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF00E5FF).copy(alpha = downMaskAlpha)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.90f * (downMaskAlpha / 0.45f)))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "METADATA",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 34.sp,
                                letterSpacing = 3.sp,
                                color = Color(0xFF00E5FF).copy(alpha = (downMaskAlpha / 0.45f).coerceIn(0f, 1f))
                            )
                        }
                    }
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
