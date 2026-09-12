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
    var width by mutableFloatStateOf(115f)
    var height by mutableFloatStateOf(110f)
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
                videoState.posX = containerWidth * 0.38f
                videoState.posY = containerHeight * 0.22f
                videoState.isInitialized = true
            }
            if (!audioState.isInitialized) {
                audioState.posX = containerWidth * 0.12f
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
                MediaType.PHOTO -> "PHOTOS"
                MediaType.VIDEO -> "VIDEOS"
                MediaType.AUDIO -> "AUDIO"
            }
            val emoji = when (b.mediaType) {
                MediaType.PHOTO -> "🖼️"
                MediaType.VIDEO -> "🎥"
                MediaType.AUDIO -> "🎧"
            }

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
                    title = title,
                    emoji = emoji,
                    count = count,
                    sizeFormatted = sizeFormatted,
                    isDragging = b.isDragging,
                    isOverShredder = isOverShredder
                )
            }
        }
    }
}

@Composable
private fun HotAirBalloonCategory(
    title: String,
    emoji: String,
    count: Int,
    sizeFormatted: String,
    isDragging: Boolean,
    isOverShredder: Boolean
) {
    val colors = LocalShredColors.current
    val strokeColor = if (isOverShredder) colors.accent else if (isDragging) colors.textPrimary else colors.border
    val fillColor = if (isOverShredder) colors.accent.copy(alpha = 0.25f) else colors.cardBackground
    val ropeColor = if (colors.isGlass) colors.accent else colors.textSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Max)
    ) {
        // 1. Teardrop Hot Air Balloon Envelope (Dome + Stripes + Emoji)
        Box(
            modifier = Modifier
                .width(115.dp)
                .height(85.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Teardrop Hot Air Balloon Envelope Path
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, h * 0.95f) // Neck bottom center
                    cubicTo(w * 0.15f, h * 0.90f, 0f, h * 0.55f, 0f, h * 0.38f) // Left taper & curve
                    cubicTo(0f, h * 0.10f, w * 0.22f, 0f, w * 0.5f, 0f) // Top left dome curve
                    cubicTo(w * 0.78f, 0f, w, h * 0.10f, w, h * 0.38f) // Top right dome curve
                    cubicTo(w, h * 0.55f, w * 0.85f, h * 0.90f, w * 0.5f, h * 0.95f) // Right taper to neck
                    close()
                }

                // Fill balloon envelope background
                drawPath(path = path, color = fillColor)

                // Draw 3D vertical stripe gores
                val stripePath1 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, 0f)
                    cubicTo(w * 0.3f, h * 0.3f, w * 0.35f, h * 0.7f, w * 0.5f, h * 0.95f)
                }
                val stripePath2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, 0f)
                    cubicTo(w * 0.7f, h * 0.3f, w * 0.65f, h * 0.7f, w * 0.5f, h * 0.95f)
                }

                drawPath(path = stripePath1, color = strokeColor.copy(alpha = 0.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
                drawPath(path = stripePath2, color = strokeColor.copy(alpha = 0.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))

                // Draw Envelope Outline
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = colors.borderWidth.toPx())
                )
            }

            // Category Emoji centered inside balloon envelope
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // 2. Suspension Ropes
        Canvas(
            modifier = Modifier
                .width(50.dp)
                .height(10.dp)
        ) {
            val w = size.width
            val h = size.height
            drawLine(color = ropeColor, start = Offset(w * 0.2f, 0f), end = Offset(w * 0.1f, h), strokeWidth = 2f)
            drawLine(color = ropeColor, start = Offset(w * 0.8f, 0f), end = Offset(w * 0.9f, h), strokeWidth = 2f)
        }

        // 3. Carriage Payload Basket (Contains ONLY Category Name, File Count & Storage Size)
        Box(
            modifier = Modifier
                .width(115.dp)
                .then(
                    if (colors.isGlass) {
                        Modifier
                            .background(colors.surface, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, colors.borderSubtle, shape = RoundedCornerShape(8.dp))
                    } else if (colors.cardCornerRadius > 0.dp) {
                        Modifier
                            .background(colors.cardBackground, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, colors.borderSubtle, shape = RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                            .background(colors.surface, shape = RoundedCornerShape(0.dp))
                            .border(2.dp, colors.border, shape = RoundedCornerShape(0.dp))
                    }
                )
                .padding(horizontal = 6.dp, vertical = 5.dp),
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
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$count FILES",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = sizeFormatted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = colors.accent
                )
            }
        }
    }
}

