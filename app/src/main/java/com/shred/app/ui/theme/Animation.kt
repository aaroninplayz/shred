package com.shred.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf

data class ShredAnimationSpec(
    // Card swipe fling-off duration in milliseconds
    val swipeFlingDurationMs: Int,
    // Card return-to-center spring physics
    val returnSpring: SpringSpec<Float>,
    // Card flip 3D animation spring physics
    val flipSpring: SpringSpec<Float>,
    // Shared element transition duration in milliseconds
    val transitionDurationMs: Int,
    // General fade/crossfade duration in milliseconds
    val crossfadeDurationMs: Int,
    // Shimmer animation cycle duration in milliseconds
    val shimmerDurationMs: Int
)

// 1. Brutalism Animation (Snappy, mechanical, instant feedback, no bounce)
val BrutalAnimationSpec = ShredAnimationSpec(
    swipeFlingDurationMs = 150,
    returnSpring = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
    flipSpring = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
    transitionDurationMs = 200,
    crossfadeDurationMs = 100,
    shimmerDurationMs = 800
)

// 2. Minimalism Animation (Smooth, elastic, springy, graceful)
val MinimalAnimationSpec = ShredAnimationSpec(
    swipeFlingDurationMs = 140,
    returnSpring = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
    flipSpring = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
    transitionDurationMs = 280,
    crossfadeDurationMs = 180,
    shimmerDurationMs = 1200
)

// 3. Glassmorphism Animation (Fluid, flowing, luminous deceleration)
val GlassAnimationSpec = ShredAnimationSpec(
    swipeFlingDurationMs = 280,
    returnSpring = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
    flipSpring = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
    transitionDurationMs = 350,
    crossfadeDurationMs = 250,
    shimmerDurationMs = 1200
)

val LocalShredAnimation = staticCompositionLocalOf { BrutalAnimationSpec }
