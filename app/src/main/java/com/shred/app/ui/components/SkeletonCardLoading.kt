package com.shred.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shred.app.ui.theme.LocalShredAnimation
import com.shred.app.ui.theme.LocalShredColors

@Composable
fun SkeletonCardLoading(
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val animSpec = LocalShredAnimation.current

    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animSpec.shimmerDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animSpec.shimmerDurationMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_pulse"
    )

    val shimmerBrush = if (colors.isGlass) {
        // Glassmorphism: Luminous translucent glow pulse
        Brush.radialGradient(
            colors = listOf(
                colors.accent.copy(alpha = 0.35f * pulseAlpha),
                colors.cardBackground.copy(alpha = 0.20f),
                colors.cardBackground.copy(alpha = 0.10f)
            ),
            radius = 600f
        )
    } else if (colors.cardCornerRadius == 0.dp) {
        // Brutalism: Stark mechanical horizontal scanline
        Brush.horizontalGradient(
            colors = listOf(
                colors.shimmerBase,
                colors.borderSubtle.copy(alpha = 0.7f),
                colors.accent.copy(alpha = 0.25f),
                colors.borderSubtle.copy(alpha = 0.7f),
                colors.shimmerBase
            ),
            startX = translateAnim - 250f,
            endX = translateAnim + 250f
        )
    } else {
        // Minimalism: Smooth diagonal gradient wave
        Brush.linearGradient(
            colors = listOf(
                colors.shimmerBase,
                colors.borderSubtle.copy(alpha = 0.45f),
                colors.shimmerBase
            ),
            start = Offset(translateAnim - 200f, translateAnim - 200f),
            end = Offset(translateAnim + 200f, translateAnim + 200f)
        )
    }

    val cardShape = RoundedCornerShape(colors.cardCornerRadius)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val targetWidth = maxWidth * 0.78f
        val targetHeightFromWidth = targetWidth / 0.66f
        val maxAllowedHeight = maxHeight * 0.80f

        val (cardWidth, cardHeight) = if (targetHeightFromWidth > maxAllowedHeight) {
            Pair(maxAllowedHeight * 0.66f, maxAllowedHeight)
        } else {
            Pair(targetWidth, targetHeightFromWidth)
        }

        // Card Deck Stack: 2nd peeking card behind
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .offset(x = 10.dp, y = 28.dp)
                .scale(0.91f)
                .alpha(0.35f)
                .border(colors.borderWidth, colors.borderSubtle.copy(alpha = 0.25f), cardShape)
                .background(colors.cardBackground, cardShape)
        )

        // Card Deck Stack: 1st peeking card behind
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .offset(x = 5.dp, y = 14.dp)
                .scale(0.955f)
                .alpha(0.65f)
                .border(colors.borderWidth, colors.borderSubtle.copy(alpha = 0.5f), cardShape)
                .background(colors.cardBackground, cardShape)
        )

        // Top Main Skeleton Card
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .border(
                    width = if (colors.borderWidth > 0.dp) colors.borderWidth else 1.dp,
                    color = if (colors.borderWidth > 0.dp) colors.border else colors.borderSubtle,
                    shape = cardShape
                )
                .background(colors.cardBackground, cardShape)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Pill Shimmer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(18.dp)
                            .background(shimmerBrush, if (colors.cardCornerRadius == 0.dp) RoundedCornerShape(0.dp) else RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(18.dp)
                            .background(shimmerBrush, if (colors.cardCornerRadius == 0.dp) RoundedCornerShape(0.dp) else RoundedCornerShape(4.dp))
                    )
                }

                // Main Center Viewport Shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 14.dp)
                        .background(shimmerBrush, if (colors.cardCornerRadius == 0.dp) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp))
                )

                // Bottom Metadata Shimmer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(16.dp)
                            .background(shimmerBrush, if (colors.cardCornerRadius == 0.dp) RoundedCornerShape(0.dp) else RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(75.dp)
                            .height(18.dp)
                            .background(shimmerBrush, if (colors.cardCornerRadius == 0.dp) RoundedCornerShape(0.dp) else RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonGridLoading(
    columns: Int = 2,
    modifier: Modifier = Modifier
) {
    val colors = LocalShredColors.current
    val animSpec = LocalShredAnimation.current

    val transition = rememberInfiniteTransition(label = "skeleton_grid_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animSpec.shimmerDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            colors.shimmerBase,
            colors.borderSubtle.copy(alpha = 0.5f),
            colors.shimmerBase
        ),
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    val cardShape = RoundedCornerShape(if (colors.cardCornerRadius > 0.dp) colors.cardCornerRadius else 8.dp)

    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(6) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardBackground, cardShape)
                    .border(1.dp, colors.borderSubtle, cardShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.15f)
                        .background(shimmerBrush, cardShape)
                )
                Column(modifier = Modifier.padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .background(shimmerBrush, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(9.dp)
                            .background(shimmerBrush, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun rememberShimmerBrush(): Brush {
    val colors = LocalShredColors.current
    val animSpec = LocalShredAnimation.current

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animSpec.shimmerDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(
            colors.shimmerBase,
            colors.borderSubtle.copy(alpha = 0.5f),
            colors.shimmerBase
        ),
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )
}

