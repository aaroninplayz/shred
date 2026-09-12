package com.shred.app.ui.screens.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredColors
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class BlockPhysicsState(
    val mediaType: MediaType,
    initialX: Float = 0f,
    initialY: Float = 0f
) {
    var posX by mutableFloatStateOf(initialX)
    var posY by mutableFloatStateOf(initialY)
    var velocityX by mutableFloatStateOf(0f)
    var velocityY by mutableFloatStateOf(0f)
    var isDragging by mutableStateOf(false)
    var width by mutableFloatStateOf(220f)
    var height by mutableFloatStateOf(95f)
    var lastDragTimestamp by mutableLongStateOf(0L)
    var lastDragDelta by mutableStateOf(Offset.Zero)
    var isInitialized by mutableStateOf(false)
}

@Composable
fun FloatingCategoryPlayground(
    photoCount: Int,
    photoSizeFormatted: String,
    videoCount: Int,
    videoSizeFormatted: String,
    audioCount: Int,
    audioSizeFormatted: String,
    containerWidth: Float,
    containerHeight: Float,
    shredderThresholdY: Float,
    onHoverShredderStateChanged: (Boolean) -> Unit,
    onThrownIntoShredder: (MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalShredColors.current

    // Accelerometer Sensor Listener
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var shakeMag by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                // Negative X so tilting phone right moves objects right
                accelX = -ax
                accelY = ay

                val mag = sqrt(ax * ax + ay * ay + az * az)
                shakeMag = mag
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && accelSensor != null) {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Initialize block physical states for PHOTO, VIDEO, AUDIO
    val photoState = remember { BlockPhysicsState(MediaType.PHOTO) }
    val videoState = remember { BlockPhysicsState(MediaType.VIDEO) }
    val audioState = remember { BlockPhysicsState(MediaType.AUDIO) }

    val blocks = remember { listOf(photoState, videoState, audioState) }

    // Sync initial positions when container layout finishes
    LaunchedEffect(containerWidth, containerHeight) {
        if (containerWidth > 0f && containerHeight > 0f) {
            if (!photoState.isInitialized) {
                photoState.posX = containerWidth * 0.08f
                photoState.posY = containerHeight * 0.05f
                photoState.isInitialized = true
            }
            if (!videoState.isInitialized) {
                videoState.posX = containerWidth * 0.38f
                videoState.posY = containerHeight * 0.25f
                videoState.isInitialized = true
            }
            if (!audioState.isInitialized) {
                audioState.posX = containerWidth * 0.12f
                audioState.posY = containerHeight * 0.45f
                audioState.isInitialized = true
            }
        }
    }

    // Central Multi-Block 2D Physics Simulation & Frame Loop (~60/120 FPS via withFrameNanos)
    LaunchedEffect(containerWidth, containerHeight, shredderThresholdY) {
        var lastTimeNanos = 0L
        var accumulatedTime = 0f

        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastTimeNanos == 0L) {
                    lastTimeNanos = frameTimeNanos
                    return@withFrameNanos
                }

                val dt = ((frameTimeNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                lastTimeNanos = frameTimeNanos
                accumulatedTime += dt

                val maxW = if (containerWidth > 0f) containerWidth else 1080f
                val maxH = if (containerHeight > 0f) containerHeight else 1920f
                val shredderTop = if (shredderThresholdY > 0f) shredderThresholdY else maxH - 240f

                val isShaken = shakeMag > 12.5f
                val shakeIntensity = if (isShaken) (shakeMag - 9.8f).coerceIn(1f, 25f) else 0f

                // 1. Update individual block physics
                blocks.forEach { b ->
                    if (!b.isDragging) {
                        val phase = b.mediaType.ordinal * 2.39f

                        // High-energy ambient sinusoidal entropy forces
                        val ambientAx = sin(accumulatedTime * 1.6f + phase) * 60f + sin(accumulatedTime * 0.8f + phase * 2f) * 30f
                        val ambientAy = (cos(accumulatedTime * 1.3f + phase.toDouble()) * 50f + cos(accumulatedTime * 0.6f + phase * 1.5f) * 25f).toFloat()

                        // Accelerometer gravity tilt forces (super responsive)
                        val tiltAx = accelX * 85f
                        val tiltAy = accelY * 85f

                        // Violent Shake Impulse
                        var shakeAx = 0f
                        var shakeAy = 0f
                        if (isShaken) {
                            shakeAx = (accelX * 90f + Random.nextFloat() * 600f - 300f) * (shakeIntensity / 8f)
                            shakeAy = (accelY * 90f + Random.nextFloat() * 600f - 300f) * (shakeIntensity / 8f)
                        }

                        b.velocityX += (ambientAx + tiltAx + shakeAx) * dt
                        b.velocityY += (ambientAy + tiltAy + shakeAy) * dt

                        // Speed clamping
                        val maxSpeed = if (isShaken) 1800f else 650f
                        val speed = sqrt(b.velocityX * b.velocityX + b.velocityY * b.velocityY)
                        if (speed > maxSpeed) {
                            b.velocityX = (b.velocityX / speed) * maxSpeed
                            b.velocityY = (b.velocityY / speed) * maxSpeed
                        }

                        b.posX += b.velocityX * dt
                        b.posY += b.velocityY * dt

                        // Smooth glidability (reduced friction)
                        b.velocityX *= 0.992f
                        b.velocityY *= 0.992f

                        // Wall Collision Resolution (Left & Right)
                        if (b.posX <= 12f) {
                            b.posX = 12f
                            b.velocityX = abs(b.velocityX) * 0.85f + 30f
                        } else if (b.posX + b.width >= maxW - 12f) {
                            b.posX = (maxW - b.width - 12f).coerceAtLeast(12f)
                            b.velocityX = -abs(b.velocityX) * 0.85f - 30f
                        }

                        // Wall Collision Resolution (Ceiling)
                        if (b.posY <= 12f) {
                            b.posY = 12f
                            b.velocityY = abs(b.velocityY) * 0.85f + 30f
                        }

                        // Shredder Floor Collision / Feed Slot
                        val cardBottom = b.posY + b.height
                        if (cardBottom >= shredderTop - 10f) {
                            if (b.velocityY > 500f || b.posY >= shredderTop + 20f) {
                                onThrownIntoShredder(b.mediaType)
                            } else {
                                b.posY = (shredderTop - b.height - 10f).coerceAtLeast(12f)
                                b.velocityY = -abs(b.velocityY) * 0.85f - 35f
                            }
                        }
                    }
                }

                // 2. Pairwise AABB Box-Box Collision Resolution (No Overlap & Elastic Bouncing)
                for (i in 0 until blocks.size) {
                    for (j in i + 1 until blocks.size) {
                        val b1 = blocks[i]
                        val b2 = blocks[j]

                        val center1X = b1.posX + b1.width / 2f
                        val center1Y = b1.posY + b1.height / 2f
                        val center2X = b2.posX + b2.width / 2f
                        val center2Y = b2.posY + b2.height / 2f

                        val dx = center1X - center2X
                        val dy = center1Y - center2Y

                        val minDistanceX = (b1.width + b2.width) / 2f
                        val minDistanceY = (b1.height + b2.height) / 2f

                        val overlapX = minDistanceX - abs(dx)
                        val overlapY = minDistanceY - abs(dy)

                        if (overlapX > 0f && overlapY > 0f) {
                            // Resolve collision along axis of minimal penetration
                            if (overlapX < overlapY) {
                                val sign = if (dx != 0f) (if (dx > 0f) 1f else -1f) else (if (i % 2 == 0) 1f else -1f)
                                val push = (overlapX / 2f) + 1f
                                if (!b1.isDragging) b1.posX = (b1.posX + push * sign).coerceIn(12f, maxW - b1.width - 12f)
                                if (!b2.isDragging) b2.posX = (b2.posX - push * sign).coerceIn(12f, maxW - b2.width - 12f)

                                val v1 = b1.velocityX
                                val v2 = b2.velocityX
                                if (!b1.isDragging) b1.velocityX = v2 * 0.85f + sign * 40f
                                if (!b2.isDragging) b2.velocityX = v1 * 0.85f - sign * 40f
                            } else {
                                val sign = if (dy != 0f) (if (dy > 0f) 1f else -1f) else (if (i % 2 == 0) 1f else -1f)
                                val push = (overlapY / 2f) + 1f
                                if (!b1.isDragging) b1.posY = (b1.posY + push * sign).coerceIn(12f, shredderTop - b1.height - 10f)
                                if (!b2.isDragging) b2.posY = (b2.posY - push * sign).coerceIn(12f, shredderTop - b2.height - 10f)

                                val v1 = b1.velocityY
                                val v2 = b2.velocityY
                                if (!b1.isDragging) b1.velocityY = v2 * 0.85f + sign * 40f
                                if (!b2.isDragging) b2.velocityY = v1 * 0.85f - sign * 40f
                            }
                        }
                    }
                }
            }
        }
    }

    val isAnyHovered = remember(blocks) {
        derivedStateOf {
            blocks.any { b -> b.isDragging && (b.posY + b.height >= shredderThresholdY - 40f) }
        }
    }.value

    LaunchedEffect(isAnyHovered) {
        onHoverShredderStateChanged(isAnyHovered)
    }

    Box(modifier = modifier.fillMaxSize()) {
        blocks.forEach { b ->
            val count = when (b.mediaType) {
                MediaType.PHOTO -> photoCount
                MediaType.VIDEO -> videoCount
                MediaType.AUDIO -> audioCount
            }
            val sizeFormatted = when (b.mediaType) {
                MediaType.PHOTO -> photoSizeFormatted
                MediaType.VIDEO -> videoSizeFormatted
                MediaType.AUDIO -> audioSizeFormatted
            }

            val title = when (b.mediaType) {
                MediaType.PHOTO -> "[ 🖼️ PHOTOS ]"
                MediaType.VIDEO -> "[ 🎥 VIDEOS ]"
                MediaType.AUDIO -> "[ 🎧 AUDIO ]"
            }

            // derivedStateOf prevents recomposition of the composable when b.posY changes during free motion
            val isOverShredder = remember(b, shredderThresholdY) {
                derivedStateOf {
                    b.isDragging && (b.posY + b.height >= shredderThresholdY - 40f)
                }
            }.value

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = b.posX
                        translationY = b.posY
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(colors.cardCornerRadius)
                        clip = true
                    }
                    .onGloballyPositioned { coords ->
                        b.width = coords.size.width.toFloat()
                        b.height = coords.size.height.toFloat()
                    }
                    .border(
                        width = colors.borderWidth,
                        color = if (isOverShredder) colors.accent else if (b.isDragging) colors.textPrimary else colors.border,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(colors.cardCornerRadius)
                    )
                    .background(
                        if (isOverShredder) colors.accent.copy(alpha = 0.15f) else if (b.isDragging) colors.surface else colors.cardBackground,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(colors.cardCornerRadius)
                    )
                    .clickable { onThrownIntoShredder(b.mediaType) }
                    .pointerInput(b.mediaType, containerWidth, containerHeight, shredderThresholdY) {
                        detectDragGestures(
                            onDragStart = {
                                b.isDragging = true
                                b.velocityX = 0f
                                b.velocityY = 0f
                                b.lastDragTimestamp = System.currentTimeMillis()
                                b.lastDragDelta = Offset.Zero
                            },
                            onDragEnd = {
                                b.isDragging = false
                                val now = System.currentTimeMillis()
                                val dt = (now - b.lastDragTimestamp).coerceAtLeast(1L) / 1000f

                                val throwVx = (b.lastDragDelta.x / dt).coerceIn(-3000f, 3000f)
                                val throwVy = (b.lastDragDelta.y / dt).coerceIn(-3000f, 3000f)
                                val cardBottom = b.posY + b.height

                                if (cardBottom >= shredderThresholdY - 50f || (throwVy > 700f && b.posY > containerHeight * 0.4f)) {
                                    onThrownIntoShredder(b.mediaType)
                                } else {
                                    b.velocityX = throwVx
                                    b.velocityY = throwVy
                                }
                            },
                            onDragCancel = {
                                b.isDragging = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                b.posX += dragAmount.x
                                b.posY += dragAmount.y
                                b.lastDragDelta = dragAmount
                                b.lastDragTimestamp = System.currentTimeMillis()
                            }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$count FILES • $sizeFormatted",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    if (isOverShredder) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RELEASE TO SHRED! ⚡",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = colors.accent
                        )
                    } else if (b.isDragging) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "▼ THROW INTO SHREDDER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
