package com.shred.app.ui.screens.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    var height by mutableFloatStateOf(160f)
    var lastDragTimestamp by mutableLongStateOf(0L)
    var lastDragDelta by mutableStateOf(Offset.Zero)
    var isInitialized by mutableStateOf(false)
}

@Composable
fun FloatingCategoryPlayground(
    photoCount: Int,
    photoSizeFormatted: String,
    photoSizeBytes: Long,
    videoCount: Int,
    videoSizeFormatted: String,
    videoSizeBytes: Long,
    audioCount: Int,
    audioSizeFormatted: String,
    audioSizeBytes: Long,
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

    // Initialize physical states for PHOTO, VIDEO, AUDIO Hot Air Balloons
    val photoState = remember { BlockPhysicsState(MediaType.PHOTO) }
    val videoState = remember { BlockPhysicsState(MediaType.VIDEO) }
    val audioState = remember { BlockPhysicsState(MediaType.AUDIO) }

    val blocks = remember { listOf(photoState, videoState, audioState) }

    // Initial positioning across screen canvas
    LaunchedEffect(containerWidth, containerHeight) {
        if (containerWidth > 0f && containerHeight > 0f) {
            if (!photoState.isInitialized) {
                photoState.posX = containerWidth * 0.05f
                photoState.posY = containerHeight * 0.05f
                photoState.isInitialized = true
            }
            if (!videoState.isInitialized) {
                videoState.posX = containerWidth * 0.36f
                videoState.posY = containerHeight * 0.22f
                videoState.isInitialized = true
            }
            if (!audioState.isInitialized) {
                audioState.posX = containerWidth * 0.10f
                audioState.posY = containerHeight * 0.42f
                audioState.isInitialized = true
            }
        }
    }

    // Average Category Size Buoyancy Physics Simulation (~60/120 FPS frame loop)
    LaunchedEffect(containerWidth, containerHeight, shredderThresholdY, photoSizeBytes, videoSizeBytes, audioSizeBytes) {
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

                // Compute Category Average Size for Buoyancy Physics
                val photoBytes = photoSizeBytes.toFloat()
                val videoBytes = videoSizeBytes.toFloat()
                val audioBytes = audioSizeBytes.toFloat()
                val avgBytes = (photoBytes + videoBytes + audioBytes) / 3f

                val maxDiff = maxOf(abs(photoBytes - avgBytes), abs(videoBytes - avgBytes), abs(audioBytes - avgBytes), 1f)

                // 1. Update individual Hot Air Balloon physics & Buoyancy
                blocks.forEach { b ->
                    if (!b.isDragging) {
                        val phase = b.mediaType.ordinal * 2.39f

                        // Ambient sinusoidal breeze forces
                        val breezeAx = sin(accumulatedTime * 1.4f + phase) * 45f + sin(accumulatedTime * 0.7f + phase * 2f) * 20f
                        val breezeAy = (cos(accumulatedTime * 1.1f + phase.toDouble()) * 30f).toFloat()

                        // Accelerometer tilt forces
                        val tiltAx = accelX * 80f
                        val tiltAy = accelY * 80f

                        // Violent Shake Impulses
                        var shakeAx = 0f
                        var shakeAy = 0f
                        if (isShaken) {
                            shakeAx = (accelX * 90f + Random.nextFloat() * 600f - 300f) * (shakeIntensity / 8f)
                            shakeAy = (accelY * 90f + Random.nextFloat() * 600f - 300f) * (shakeIntensity / 8f)
                        }

                        // Buoyancy Differential relative to Average Category Size
                        val catBytes = when (b.mediaType) {
                            MediaType.PHOTO -> photoBytes
                            MediaType.VIDEO -> videoBytes
                            MediaType.AUDIO -> audioBytes
                        }
                        val diffBytes = catBytes - avgBytes
                        val normalizedRatio = (diffBytes / maxDiff).coerceIn(-1f, 1f)

                        // Heavier than average -> sinks downward (+Y). Lighter than average -> floats upward (-Y)
                        val buoyancyAy = normalizedRatio * 210f

                        b.velocityX += (breezeAx + tiltAx + shakeAx) * dt
                        b.velocityY += (breezeAy + tiltAy + shakeAy + buoyancyAy) * dt

                        // Speed clamping
                        val maxSpeed = if (isShaken) 1800f else 600f
                        val speed = sqrt(b.velocityX * b.velocityX + b.velocityY * b.velocityY)
                        if (speed > maxSpeed) {
                            b.velocityX = (b.velocityX / speed) * maxSpeed
                            b.velocityY = (b.velocityY / speed) * maxSpeed
                        }

                        b.posX += b.velocityX * dt
                        b.posY += b.velocityY * dt

                        // Smooth glidability
                        b.velocityX *= 0.991f
                        b.velocityY *= 0.991f

                        // Wall Collision Resolution (Left & Right)
                        if (b.posX <= 12f) {
                            b.posX = 12f
                            b.velocityX = abs(b.velocityX) * 0.85f + 30f
                        } else if (b.posX + b.width >= maxW - 12f) {
                            b.posX = (maxW - b.width - 12f).coerceAtLeast(12f)
                            b.velocityX = -abs(b.velocityX) * 0.85f - 30f
                        }

                        // Wall Collision Resolution (Ceiling Sky)
                        if (b.posY <= 12f) {
                            b.posY = 12f
                            b.velocityY = abs(b.velocityY) * 0.85f + 30f
                        }

                        // Shredder Floor Collision / Feed Slot
                        val balloonBottom = b.posY + b.height
                        if (balloonBottom >= shredderTop - 10f) {
                            if (b.velocityY > 500f || b.posY >= shredderTop + 20f) {
                                onThrownIntoShredder(b.mediaType)
                            } else {
                                b.posY = (shredderTop - b.height - 10f).coerceAtLeast(12f)
                                b.velocityY = -abs(b.velocityY) * 0.85f - 35f
                            }
                        }
                    }
                }

                // 2. Pairwise AABB Collision Resolution (Zero Overlap & Elastic Bouncing)
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

    val photoBytes = photoSizeBytes.toFloat()
    val videoBytes = videoSizeBytes.toFloat()
    val audioBytes = audioSizeBytes.toFloat()
    val avgBytes = (photoBytes + videoBytes + audioBytes) / 3f

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
            val catBytes = when (b.mediaType) {
                MediaType.PHOTO -> photoBytes
                MediaType.VIDEO -> videoBytes
                MediaType.AUDIO -> audioBytes
            }

            val title = when (b.mediaType) {
                MediaType.PHOTO -> "PHOTOS"
                MediaType.VIDEO -> "VIDEOS"
                MediaType.AUDIO -> "AUDIO"
            }
            val emoji = when (b.mediaType) {
                MediaType.PHOTO -> "🖼️"
                MediaType.VIDEO -> "🎥"
                MediaType.AUDIO -> "🎧"
            }

            val isSinking = catBytes > avgBytes + 1024f
            val isFloating = catBytes < avgBytes - 1024f

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
                    }
                    .onGloballyPositioned { coords ->
                        b.width = coords.size.width.toFloat()
                        b.height = coords.size.height.toFloat()
                    }
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
                                val balloonBottom = b.posY + b.height

                                if (balloonBottom >= shredderThresholdY - 50f || (throwVy > 700f && b.posY > containerHeight * 0.4f)) {
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
            ) {
                HotAirBalloonCategory(
                    mediaType = b.mediaType,
                    title = title,
                    emoji = emoji,
                    count = count,
                    sizeFormatted = sizeFormatted,
                    isSinking = isSinking,
                    isFloating = isFloating,
                    isDragging = b.isDragging,
                    isOverShredder = isOverShredder
                )
            }
        }
    }
}

@Composable
private fun HotAirBalloonCategory(
    mediaType: MediaType,
    title: String,
    emoji: String,
    count: Int,
    sizeFormatted: String,
    isSinking: Boolean,
    isFloating: Boolean,
    isDragging: Boolean,
    isOverShredder: Boolean
) {
    val colors = LocalShredColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Max)
    ) {
        // 1. Hot Air Balloon Envelope (Dome)
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(115.dp)
                .then(
                    if (colors.isGlass) {
                        Modifier
                            .shadow(16.dp, RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp, bottomStart = 24.dp, bottomEnd = 24.dp), ambientColor = colors.accent, spotColor = colors.accent)
                            .background(
                                colors.cardBackground,
                                shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isOverShredder) colors.accent else if (isDragging) colors.textPrimary else colors.border,
                                shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                    } else if (colors.cardCornerRadius > 0.dp) {
                        // Minimalism
                        Modifier
                            .shadow(8.dp, RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                            .background(
                                colors.surface,
                                shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isOverShredder) colors.accent else if (isDragging) colors.textPrimary else colors.borderSubtle,
                                shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                            )
                    } else {
                        // Brutalism
                        Modifier
                            .background(
                                if (isOverShredder) colors.accent.copy(alpha = 0.2f) else colors.cardBackground,
                                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                            )
                            .border(
                                width = 3.dp,
                                color = if (isOverShredder) colors.accent else colors.border,
                                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                            )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(emoji, fontSize = 16.sp)
                    Text(
                        text = if (colors.cardCornerRadius == 0.dp) "[ $title ]" else title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Buoyancy Status Indicator Badge
                val buoyancyBadge = when {
                    isOverShredder -> "RELEASE TO SHRED! ⚡"
                    isDragging -> "▼ THROW DOWN TO SHRED"
                    isSinking -> "HEAVY • SINKS ⚓"
                    isFloating -> "LIGHT • FLOATS 🎈"
                    else -> "NEUTRAL BUOYANCY ⚖️"
                }

                val buoyancyColor = when {
                    isOverShredder -> colors.accent
                    isSinking -> Color(0xFFFF5252)
                    isFloating -> Color(0xFF69F0AE)
                    else -> colors.textSecondary
                }

                Text(
                    text = buoyancyBadge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = buoyancyColor
                )
            }
        }

        // 2. Suspension Ropes & Burner Glow
        val ropeColor = if (colors.isGlass) colors.accent else colors.textSecondary
        Canvas(
            modifier = Modifier
                .width(180.dp)
                .height(16.dp)
        ) {
            val w = size.width
            val h = size.height
            // Draw left and right ropes connecting balloon envelope to payload basket
            drawLine(color = ropeColor, start = Offset(w * 0.25f, 0f), end = Offset(w * 0.35f, h), strokeWidth = 2f)
            drawLine(color = ropeColor, start = Offset(w * 0.75f, 0f), end = Offset(w * 0.65f, h), strokeWidth = 2f)
        }

        // 3. Payload Basket
        Box(
            modifier = Modifier
                .width(200.dp)
                .then(
                    if (colors.isGlass) {
                        Modifier
                            .background(colors.surface, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, colors.borderSubtle, shape = RoundedCornerShape(12.dp))
                    } else if (colors.cardCornerRadius > 0.dp) {
                        Modifier
                            .background(colors.cardBackground, shape = RoundedCornerShape(10.dp))
                            .border(1.dp, colors.borderSubtle, shape = RoundedCornerShape(10.dp))
                    } else {
                        Modifier
                            .background(colors.surface, shape = RoundedCornerShape(0.dp))
                            .border(2.dp, colors.border, shape = RoundedCornerShape(0.dp))
                    }
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count FILES • $sizeFormatted",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
    }
}

