package com.shred.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shred.app.data.model.ThemeMode


data class ShredThemeColors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val cardCornerRadius: Dp = 0.dp,
    val borderWidth: Dp = 3.dp,
    val isGlass: Boolean = false,
    val shadowElevation: Dp = 0.dp
)

val LocalShredColors = staticCompositionLocalOf {
    ShredThemeColors(
        background = BrutalBackground,
        surface = BrutalSurface,
        cardBackground = BrutalCard,
        border = BrutalBorder,
        borderSubtle = BrutalBorderSubtle,
        textPrimary = BrutalTextPrimary,
        textSecondary = BrutalTextSecondary,
        accent = BrutalAccent,
        cardCornerRadius = 0.dp,
        borderWidth = 3.dp,
        isGlass = false,
        shadowElevation = 0.dp
    )
}

@Composable
fun ShredTheme(
    themeMode: ThemeMode = ThemeMode.BRUTALISM,
    content: @Composable () -> Unit
) {
    val shredColors = when (themeMode) {
        ThemeMode.BRUTALISM -> ShredThemeColors(
            background = BrutalBackground,
            surface = BrutalSurface,
            cardBackground = BrutalCard,
            border = BrutalBorder,
            borderSubtle = BrutalBorderSubtle,
            textPrimary = BrutalTextPrimary,
            textSecondary = BrutalTextSecondary,
            accent = BrutalAccent,
            cardCornerRadius = 0.dp,
            borderWidth = 3.dp,
            isGlass = false,
            shadowElevation = 0.dp
        )
        ThemeMode.MINIMALISM -> ShredThemeColors(
            background = MinimalBackground,
            surface = MinimalSurface,
            cardBackground = MinimalCard,
            border = MinimalBorder,
            borderSubtle = MinimalBorderSubtle,
            textPrimary = MinimalTextPrimary,
            textSecondary = MinimalTextSecondary,
            accent = MinimalAccent,
            cardCornerRadius = 16.dp,
            borderWidth = 1.dp,
            isGlass = false,
            shadowElevation = 4.dp
        )
        ThemeMode.GLASSMORPHISM -> ShredThemeColors(
            background = GlassBackground,
            surface = GlassSurface,
            cardBackground = GlassCard,
            border = GlassBorder,
            borderSubtle = GlassBorderSubtle,
            textPrimary = GlassTextPrimary,
            textSecondary = GlassTextSecondary,
            accent = GlassAccent,
            cardCornerRadius = 24.dp,
            borderWidth = 1.5.dp,
            isGlass = true,
            shadowElevation = 12.dp
        )
    }

    val materialColors = darkColorScheme(
        background = shredColors.background,
        surface = shredColors.surface,
        primary = shredColors.accent,
        onBackground = shredColors.textPrimary,
        onSurface = shredColors.textPrimary
    )

    val shapes = when (themeMode) {
        ThemeMode.BRUTALISM -> BrutalShapes
        ThemeMode.MINIMALISM -> MinimalShapes
        ThemeMode.GLASSMORPHISM -> GlassShapes
    }

    CompositionLocalProvider(LocalShredColors provides shredColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = BrutalTypography,
            shapes = shapes,
            content = content
        )
    }
}

