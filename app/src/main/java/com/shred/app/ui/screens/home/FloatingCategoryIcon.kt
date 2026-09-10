package com.shred.app.ui.screens.home

import androidx.compose.animation.core.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shred.app.data.model.MediaType
import com.shred.app.ui.theme.LocalShredColors
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun FloatingCategoryBlock(
    mediaType: MediaType,
    count: Int,
    sizeFormatted: String,
    initialOffsetX: Float,
    initialOffsetY: Float,
    shredderThresholdY: Float,
    onThrownIntoShredder: (MediaType) -> Unit
) {
    val colors = LocalShredColors.current

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "floating_$mediaType")

    val driftX by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400 + (mediaType.ordinal * 600), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )

    val driftY by infiniteTransition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800 + (mediaType.ordinal * 500), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    // Compute active position
    val currentY = if (isDragging) {
        initialOffsetY + dragOffset.y
    } else {
        // Scared physics: if initial + drift enters lower area, bounce back up
        val calculated = initialOffsetY + driftY
        if (calculated > shredderThresholdY - 120f) shredderThresholdY - 140f else calculated
    }

    val currentX = if (isDragging) {
        initialOffsetX + dragOffset.x
    } else {
        initialOffsetX + driftX
    }

    val title = when (mediaType) {
        MediaType.PHOTO -> "[ 🖼️ PHOTOS ]"
        MediaType.VIDEO -> "[ 🎥 VIDEOS ]"
        MediaType.AUDIO -> "[ 🎧 AUDIO ]"
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
            .border(2.5.dp, if (isDragging) colors.accent else colors.border)
            .background(if (isDragging) colors.surface else colors.cardBackground)
            .clickable { onThrownIntoShredder(mediaType) }
            .pointerInput(mediaType) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffset = Offset.Zero
                    },
                    onDragEnd = {
                        isDragging = false
                        // Check if dragged down into the shredder area
                        if (currentY >= shredderThresholdY - 60f) {
                            onThrownIntoShredder(mediaType)
                        }
                        dragOffset = Offset.Zero
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                )
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
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
            if (isDragging) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "▼ FLICK TO SHRED",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    color = colors.accent
                )
            }
        }
    }
}
