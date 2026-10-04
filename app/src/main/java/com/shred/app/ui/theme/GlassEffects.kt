package com.shred.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Iridescent dual-rim gradient brush (Cyan -> Magenta / Violet) for Glassmorphism.
 */
@Composable
fun glassBorderBrush(
    startColor: Color = LocalShredColors.current.border,
    endColor: Color = LocalShredColors.current.borderSubtle
): Brush {
    return Brush.linearGradient(
        colors = listOf(startColor, endColor)
    )
}

/**
 * Modifier extension applying Cosmic Aurora Glassmorphism styling:
 * - Ambient drop shadow
 * - Translucent frosted fill
 * - Iridescent dual-rim glowing border
 * - Rounded corners (default 20.dp)
 */
fun Modifier.glassPanel(
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = Color(0x18FFFFFF),
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 8.dp,
    ambientGlowColor: Color = Color(0x3300F2FE),
    spotGlowColor: Color = Color(0x22FF007F)
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveBorderBrush = borderBrush ?: Brush.linearGradient(
        listOf(Color(0x6600F2FE), Color(0x40FF007F))
    )

    return this
        .shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = ambientGlowColor,
            spotColor = spotGlowColor
        )
        .clip(shape)
        .background(backgroundColor, shape)
        .border(
            border = BorderStroke(borderWidth, effectiveBorderBrush),
            shape = shape
        )
}
