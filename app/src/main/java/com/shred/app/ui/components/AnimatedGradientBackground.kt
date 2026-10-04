package com.shred.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance animated cosmic aurora background for Glassmorphism theme.
 * Renders shifting, drifting luminous nebulae (Cyan, Neon Magenta, Deep Indigo)
 * via hardware-accelerated Canvas radial gradients.
 */
@Composable
fun AnimatedGradientBackground(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "auroraTransition")

    // Slow orbital rotation for primary nebula (24 seconds)
    val angleRad by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraAngle"
    )

    // Gentle breathing scale for luminous nodes (8 seconds)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraPulse"
    )

    // Secondary drift offset (16 seconds)
    val driftFactor by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraDrift"
    )

    // Cosmic base palette
    val baseColor = if (isDarkMode) Color(0xFF070914) else Color(0xFFEEF2F7)
    val cyanGlow = if (isDarkMode) Color(0x3500F2FE) else Color(0x250284C7)
    val magentaGlow = if (isDarkMode) Color(0x30FF007F) else Color(0x20D946EF)
    val indigoGlow = if (isDarkMode) Color(0x403B0764) else Color(0x18818CF8)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw base cosmic fill
            drawRect(color = baseColor)

            if (width <= 0f || height <= 0f) return@Canvas

            // 2. Cyan Nebula (orbiting around top quadrant)
            val cyanCenterX = (width * 0.35f) + (cos(angleRad) * width * 0.22f)
            val cyanCenterY = (height * 0.25f) + (sin(angleRad) * height * 0.15f)
            val cyanRadius = (width.coerceAtLeast(height) * 0.55f) * pulseScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cyanGlow, Color.Transparent),
                    center = Offset(cyanCenterX, cyanCenterY),
                    radius = cyanRadius
                ),
                center = Offset(cyanCenterX, cyanCenterY),
                radius = cyanRadius
            )

            // 3. Magenta Nebula (counter-orbiting around bottom-right quadrant)
            val magentaCenterX = (width * 0.70f) - (cos(angleRad * 0.75f) * width * 0.25f)
            val magentaCenterY = (height * 0.72f) - (sin(angleRad * 0.75f) * height * 0.18f)
            val magentaRadius = (width.coerceAtLeast(height) * 0.60f) * (2.1f - pulseScale)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(magentaGlow, Color.Transparent),
                    center = Offset(magentaCenterX, magentaCenterY),
                    radius = magentaRadius
                ),
                center = Offset(magentaCenterX, magentaCenterY),
                radius = magentaRadius
            )

            // 4. Indigo / Deep Violet Center-Left Glow
            val indigoCenterX = (width * (0.20f + driftFactor))
            val indigoCenterY = (height * (0.55f - driftFactor))
            val indigoRadius = (width.coerceAtLeast(height) * 0.45f) * pulseScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(indigoGlow, Color.Transparent),
                    center = Offset(indigoCenterX, indigoCenterY),
                    radius = indigoRadius
                ),
                center = Offset(indigoCenterX, indigoCenterY),
                radius = indigoRadius
            )
        }

        // Render screen content on top of luminous aurora
        content()
    }
}
