package com.shred.app.ui.screens.swipe

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.ActionType
import com.shred.app.data.model.MediaFile
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredAnimation
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.SwipeDeleteRed
import com.shred.app.ui.theme.SwipeFavoriteGold
import com.shred.app.ui.theme.SwipeKeepGreen
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class GestureLock {
    NONE,
    HORIZONTAL,
    VERTICAL_UP,
    VERTICAL_DOWN
}

@Composable
fun MediaCard(
    file: MediaFile,
    isTopCard: Boolean,
    onSwiped: (ActionType) -> Unit,
    onUndo: (() -> Unit)? = null,
    onDragDelta: (Float, Float) -> Unit = { _, _ -> },
    onFolderLongPress: (() -> Unit)? = null,
    onZoomModeChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val animSpec = LocalShredAnimation.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val offsetX = remember(file.id) { Animatable(0f) }
    val offsetY = remember(file.id) { Animatable(0f) }
    val flipProgress = remember(file.id) { Animatable(0f) } // 0f = front, 180f = back
    val velocityTracker = remember(file.id) { VelocityTracker() }

    // Tracking axis lock and gesture limits
    var gestureLock by remember(file.id) { mutableStateOf(GestureLock.NONE) }
    var accumulatedDeltaX by remember(file.id) { mutableFloatStateOf(0f) }
    var accumulatedDeltaY by remember(file.id) { mutableFloatStateOf(0f) }
    var verticalDragUp by remember(file.id) { mutableFloatStateOf(0f) }
    var verticalDragDown by remember(file.id) { mutableFloatStateOf(0f) }
    var isEdgeDrag by remember(file.id) { mutableStateOf(false) }
    var edgeDragProgress by remember(file.id) { mutableFloatStateOf(0f) }

    var isFlipped by remember(file.id) { mutableStateOf(false) }
    var isZoomMode by remember(file.id) { mutableStateOf(false) }

    LaunchedEffect(isZoomMode) {
        onZoomModeChanged?.invoke(isZoomMode)
    }

    val rotationZ = if (isFlipped) 0f else (offsetX.value / 32f).coerceIn(-28f, 28f)
    val arcY = if (!isFlipped && abs(offsetX.value) > 0f) -abs(offsetX.value) * 0.10f else 0f
    val effectiveOffsetY = offsetY.value + arcY

    val tolerancePx = with(density) { 24.dp.toPx() }
    val maxDragX = with(density) { 220.dp.toPx() }
    val maxDragUp = with(density) { 220.dp.toPx() }
    val maxDragDown = with(density) { 150.dp.toPx() }
    val commitThresholdX = with(density) { 130.dp.toPx() }
    val commitThresholdUp = with(density) { 130.dp.toPx() }
    val commitThresholdDown = with(density) { 85.dp.toPx() }

    // GTA Action Banner state (WASTED, SAVED, RESPECT +, METADATA)
    val dragX = offsetX.value
    val absDragX = abs(dragX)
    val dragY = offsetY.value

    data class GtaBanner(val text: String, val color: Color, val alpha: Float)
    val gtaBanner: GtaBanner? = when {
        // Left drag -> WASTED (Delete)
        dragX < -30f && (gestureLock == GestureLock.HORIZONTAL || absDragX > abs(dragY)) -> {
            val alpha = (absDragX / commitThresholdX).coerceIn(0f, 1f)
            GtaBanner("WASTED", SwipeDeleteRed, alpha)
        }
        // Right drag -> SAVED (Keep)
        dragX > 30f && (gestureLock == GestureLock.HORIZONTAL || absDragX > abs(dragY)) -> {
            val alpha = (absDragX / commitThresholdX).coerceIn(0f, 1f)
            GtaBanner("SAVED", SwipeKeepGreen, alpha)
        }
        // Up drag -> RESPECT + (Favorite)
        dragY < -30f && (gestureLock == GestureLock.VERTICAL_UP || dragY < 0f) && !isFlipped -> {
            val alpha = (abs(dragY) / commitThresholdUp).coerceIn(0f, 1f)
            GtaBanner("RESPECT +", SwipeFavoriteGold, alpha)
        }
        // Down drag -> METADATA (Flip to back)
        dragY > 20f && (gestureLock == GestureLock.VERTICAL_DOWN || dragY > 0f) && !isFlipped -> {
            val alpha = (dragY / commitThresholdDown).coerceIn(0f, 1f)
            GtaBanner("METADATA", Color(0xFF00E5FF), alpha)
        }
        else -> null
    }

    // Down drag -> Progressive Grayscale
    val grayscaleProgress = if (!isFlipped && verticalDragDown > 10f) {
        (verticalDragDown / maxDragDown).coerceIn(0f, 0.5f) + (flipProgress.value / 180f * 0.5f)
    } else if (isFlipped) {
        1.0f
    } else {
        0.0f
    }

    val edgeThresholdPx = with(density) { 36.dp.toPx() }
    val cardShape = RoundedCornerShape(colors.cardCornerRadius)
    val flipAngle = flipProgress.value
    val sinAngle = abs(kotlin.math.sin(Math.toRadians(flipAngle.toDouble()))).toFloat()

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.value.roundToInt(), effectiveOffsetY.roundToInt()) }
            .graphicsLayer {
                this.rotationZ = rotationZ
                this.rotationX = flipAngle
                this.cameraDistance = 22f * density.density
                this.transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
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
                        onFolderLongPress?.invoke()
                    }
                )
            }
            .pointerInput(file.id, isTopCard, isFlipped, isZoomMode) {
                if (!isTopCard || isZoomMode) return@pointerInput

                detectDragGestures(
                    onDragStart = { startOffset ->
                        velocityTracker.resetTracking()
                        velocityTracker.addPosition(System.currentTimeMillis(), startOffset)
                        isEdgeDrag = startOffset.x <= edgeThresholdPx
                        edgeDragProgress = 0f
                        gestureLock = GestureLock.NONE
                        accumulatedDeltaX = 0f
                        accumulatedDeltaY = 0f
                        verticalDragUp = 0f
                        verticalDragDown = 0f
                    },
                    onDragEnd = {
                        scope.launch {
                            // Check left-edge swipe undo
                            if (isEdgeDrag && edgeDragProgress >= 100f) {
                                onUndo?.invoke()
                                isEdgeDrag = false
                                edgeDragProgress = 0f
                                launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                return@launch
                            }
                            isEdgeDrag = false
                            edgeDragProgress = 0f

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
                                gestureLock = GestureLock.NONE
                            } else {
                                val velocity = velocityTracker.calculateVelocity()
                                val vx = velocity.x
                                val vy = velocity.y
                                val flingThresholdPx = with(density) { 950.dp.toPx() } // intentional fast flick (~2500 px/s)
                                val minFlingDistX = with(density) { 45.dp.toPx() }
                                val minFlingDistUp = with(density) { 45.dp.toPx() }
                                val minFlingDistDown = with(density) { 35.dp.toPx() }

                                when (gestureLock) {
                                    GestureLock.HORIZONTAL -> {
                                        val currentX = offsetX.value
                                        val isFlingRight = vx > flingThresholdPx && currentX >= minFlingDistX
                                        val isFlingLeft = vx < -flingThresholdPx && currentX <= -minFlingDistX

                                        if (currentX > commitThresholdX || isFlingRight) {
                                            offsetX.animateTo(2200f, tween(130))
                                            onSwiped(ActionType.KEEP)
                                        } else if (currentX < -commitThresholdX || isFlingLeft) {
                                            offsetX.animateTo(-2200f, tween(130))
                                            onSwiped(ActionType.DELETE)
                                        } else {
                                            launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                        }
                                    }
                                    GestureLock.VERTICAL_UP -> {
                                        val currentY = offsetY.value
                                        val isFlingUp = vy < -flingThresholdPx && currentY <= -minFlingDistUp

                                        if (abs(currentY) > commitThresholdUp || isFlingUp) {
                                            offsetY.animateTo(-2400f, tween(130))
                                            onSwiped(ActionType.FAVORITE)
                                        } else {
                                            launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                        }
                                    }
                                    GestureLock.VERTICAL_DOWN -> {
                                        val currentY = offsetY.value
                                        val isFlingDown = vy > (flingThresholdPx * 0.85f) && currentY >= minFlingDistDown

                                        if (currentY >= commitThresholdDown || isFlingDown) {
                                            launch { offsetY.animateTo(0f, tween(260)) }
                                            launch { flipProgress.animateTo(180f, tween(320)) }
                                            isFlipped = true
                                        } else {
                                            launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                            launch { flipProgress.animateTo(0f, animSpec.flipSpring) }
                                        }
                                    }
                                    GestureLock.NONE -> {
                                        val currentX = offsetX.value
                                        val currentY = offsetY.value
                                        if (abs(vx) > abs(vy) && abs(vx) > flingThresholdPx) {
                                            if (vx > 0 && currentX >= minFlingDistX) {
                                                offsetX.animateTo(2200f, tween(130))
                                                onSwiped(ActionType.KEEP)
                                            } else if (vx < 0 && currentX <= -minFlingDistX) {
                                                offsetX.animateTo(-2200f, tween(130))
                                                onSwiped(ActionType.DELETE)
                                            } else {
                                                launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                                launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                            }
                                        } else if (abs(vy) > flingThresholdPx) {
                                            if (vy < 0 && currentY <= -minFlingDistUp) {
                                                offsetY.animateTo(-2400f, tween(130))
                                                onSwiped(ActionType.FAVORITE)
                                            } else if (vy > 0 && currentY >= minFlingDistDown) {
                                                launch { offsetY.animateTo(0f, tween(260)) }
                                                launch { flipProgress.animateTo(180f, tween(320)) }
                                                isFlipped = true
                                            } else {
                                                launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                                launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                            }
                                        } else {
                                            launch { offsetX.animateTo(0f, animSpec.returnSpring) }
                                            launch { offsetY.animateTo(0f, animSpec.returnSpring) }
                                        }
                                    }
                                }
                                verticalDragUp = 0f
                                verticalDragDown = 0f
                                gestureLock = GestureLock.NONE
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
                            isEdgeDrag = false
                            edgeDragProgress = 0f
                            gestureLock = GestureLock.NONE
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch {
                            if (isEdgeDrag) {
                                edgeDragProgress += dragAmount.x
                                return@launch
                            }

                            if (!isFlipped) {
                                if (gestureLock == GestureLock.NONE) {
                                    accumulatedDeltaX += dragAmount.x
                                    accumulatedDeltaY += dragAmount.y
                                    val distSq = accumulatedDeltaX * accumulatedDeltaX + accumulatedDeltaY * accumulatedDeltaY
                                    if (distSq >= tolerancePx * tolerancePx) {
                                        if (abs(accumulatedDeltaX) >= abs(accumulatedDeltaY)) {
                                            gestureLock = GestureLock.HORIZONTAL
                                            offsetY.snapTo(0f)
                                        } else if (accumulatedDeltaY < 0f) {
                                            gestureLock = GestureLock.VERTICAL_UP
                                            offsetX.snapTo(0f)
                                        } else {
                                            gestureLock = GestureLock.VERTICAL_DOWN
                                            offsetX.snapTo(0f)
                                        }
                                    } else {
                                        // Slight tactile responsiveness in center deadzone
                                        offsetX.snapTo(accumulatedDeltaX * 0.35f)
                                        offsetY.snapTo(accumulatedDeltaY * 0.35f)
                                    }
                                }

                                when (gestureLock) {
                                    GestureLock.HORIZONTAL -> {
                                        val nextX = (offsetX.value + dragAmount.x).coerceIn(-maxDragX, maxDragX)
                                        offsetX.snapTo(nextX)
                                        offsetY.snapTo(0f)
                                        onDragDelta(offsetX.value, 0f)
                                    }
                                    GestureLock.VERTICAL_UP -> {
                                        val nextY = (offsetY.value + dragAmount.y).coerceIn(-maxDragUp, 0f)
                                        offsetY.snapTo(nextY)
                                        verticalDragUp = abs(nextY)
                                        offsetX.snapTo(0f)
                                        onDragDelta(0f, offsetY.value)
                                    }
                                    GestureLock.VERTICAL_DOWN -> {
                                        val nextY = (offsetY.value + dragAmount.y).coerceIn(0f, maxDragDown)
                                        offsetY.snapTo(nextY)
                                        verticalDragDown = nextY
                                        offsetX.snapTo(0f)
                                    }
                                    GestureLock.NONE -> { /* waiting for tolerance */ }
                                }
                            } else {
                                // Upward drag flips card back to front
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
            // Back of card (Metadata) - upside-down flip compensation
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
            // Front of card (Media Content)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        // Progressive grayscale filter on down drag
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

                // 1. Full Progressive Action Color Tint Overlay + Cinema Punchout Banner
                if (gtaBanner != null && gtaBanner.alpha > 0.05f) {
                    val overlayAlpha = (gtaBanner.alpha * 0.40f).coerceIn(0f, 0.50f)
                    // Full-card action color tint
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gtaBanner.color.copy(alpha = overlayAlpha)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Cinema punch-through bar with solid black background and sharp action text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.92f * gtaBanner.alpha))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gtaBanner.text,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 34.sp,
                                letterSpacing = 3.sp,
                                color = gtaBanner.color.copy(alpha = gtaBanner.alpha)
                            )
                        }
                    }
                }

                // 2. Left-Edge Swipe Undo Visual Indicator
                if (isEdgeDrag && edgeDragProgress > 10f) {
                    val undoAlpha = (edgeDragProgress / 100f).coerceIn(0.2f, 1f)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(6.dp)
                            .fillMaxHeight(0.6f)
                            .graphicsLayer { alpha = undoAlpha }
                            .background(colors.accent)
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
