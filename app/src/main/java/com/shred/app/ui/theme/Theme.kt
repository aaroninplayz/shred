package com.shred.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shred.app.data.model.ColorMode
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
    val shadowElevation: Dp = 0.dp,
    val accentSecondary: Color = accent,
    val overlayBackground: Color = Color.Black.copy(alpha = 0.5f),
    val shimmerBase: Color = surface,
    val shimmerHighlight: Color = cardBackground,
    val invertedFill: Color = textPrimary,
    val invertedText: Color = background,
    val isDark: Boolean = true
)

val LocalShredColors = staticCompositionLocalOf {
    ShredThemeColors(
        background = BrutalDarkBackground,
        surface = BrutalDarkSurface,
        cardBackground = BrutalDarkCard,
        border = BrutalDarkBorder,
        borderSubtle = BrutalDarkBorderSubtle,
        textPrimary = BrutalDarkTextPrimary,
        textSecondary = BrutalDarkTextSecondary,
        accent = BrutalDarkAccent,
        cardCornerRadius = 0.dp,
        borderWidth = 3.dp,
        isGlass = false,
        shadowElevation = 0.dp,
        invertedFill = BrutalDarkInvertedFill,
        invertedText = BrutalDarkInvertedText,
        isDark = true
    )
}

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.BRUTALISM }
val LocalColorMode = staticCompositionLocalOf { ColorMode.SYSTEM }

@Composable
fun ShredTheme(
    themeMode: ThemeMode = ThemeMode.BRUTALISM,
    colorMode: ColorMode = ColorMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (colorMode) {
        ColorMode.LIGHT -> false
        ColorMode.DARK -> true
        ColorMode.SYSTEM -> isSystemInDarkTheme()
    }

    val shredColors = when (themeMode) {
        ThemeMode.BRUTALISM -> if (isDark) {
            ShredThemeColors(
                background = BrutalDarkBackground,
                surface = BrutalDarkSurface,
                cardBackground = BrutalDarkCard,
                border = BrutalDarkBorder,
                borderSubtle = BrutalDarkBorderSubtle,
                textPrimary = BrutalDarkTextPrimary,
                textSecondary = BrutalDarkTextSecondary,
                accent = BrutalDarkAccent,
                cardCornerRadius = 0.dp,
                borderWidth = 3.dp,
                isGlass = false,
                shadowElevation = 0.dp,
                invertedFill = BrutalDarkInvertedFill,
                invertedText = BrutalDarkInvertedText,
                isDark = true
            )
        } else {
            ShredThemeColors(
                background = BrutalLightBackground,
                surface = BrutalLightSurface,
                cardBackground = BrutalLightCard,
                border = BrutalLightBorder,
                borderSubtle = BrutalLightBorderSubtle,
                textPrimary = BrutalLightTextPrimary,
                textSecondary = BrutalLightTextSecondary,
                accent = BrutalLightAccent,
                cardCornerRadius = 0.dp,
                borderWidth = 3.dp,
                isGlass = false,
                shadowElevation = 0.dp,
                invertedFill = BrutalLightInvertedFill,
                invertedText = BrutalLightInvertedText,
                isDark = false
            )
        }

        ThemeMode.MINIMALISM -> if (isDark) {
            ShredThemeColors(
                background = MinimalDarkBackground,
                surface = MinimalDarkSurface,
                cardBackground = MinimalDarkCard,
                border = MinimalDarkBorder,
                borderSubtle = MinimalDarkBorderSubtle,
                textPrimary = MinimalDarkTextPrimary,
                textSecondary = MinimalDarkTextSecondary,
                accent = MinimalDarkAccent,
                cardCornerRadius = 16.dp,
                borderWidth = 0.dp,
                isGlass = false,
                shadowElevation = 6.dp,
                invertedFill = MinimalDarkAccent,
                invertedText = Color(0xFF0F1115),
                isDark = true
            )
        } else {
            ShredThemeColors(
                background = MinimalLightBackground,
                surface = MinimalLightSurface,
                cardBackground = MinimalLightCard,
                border = MinimalLightBorder,
                borderSubtle = MinimalLightBorderSubtle,
                textPrimary = MinimalLightTextPrimary,
                textSecondary = MinimalLightTextSecondary,
                accent = MinimalLightAccent,
                cardCornerRadius = 16.dp,
                borderWidth = 0.dp,
                isGlass = false,
                shadowElevation = 6.dp,
                invertedFill = MinimalLightAccent,
                invertedText = Color.White,
                isDark = false
            )
        }

        ThemeMode.GLASSMORPHISM -> if (isDark) {
            ShredThemeColors(
                background = GlassDarkBackground,
                surface = GlassDarkSurface,
                cardBackground = GlassDarkCard,
                border = GlassDarkBorder,
                borderSubtle = GlassDarkBorderSubtle,
                textPrimary = GlassDarkTextPrimary,
                textSecondary = GlassDarkTextSecondary,
                accent = GlassDarkAccent,
                accentSecondary = GlassDarkAccentSecondary,
                cardCornerRadius = 20.dp,
                borderWidth = 1.dp,
                isGlass = true,
                shadowElevation = 8.dp,
                invertedFill = GlassDarkAccent,
                invertedText = Color.Black,
                isDark = true
            )
        } else {
            ShredThemeColors(
                background = GlassLightBackground,
                surface = GlassLightSurface,
                cardBackground = GlassLightCard,
                border = GlassLightBorder,
                borderSubtle = GlassLightBorderSubtle,
                textPrimary = GlassLightTextPrimary,
                textSecondary = GlassLightTextSecondary,
                accent = GlassLightAccent,
                accentSecondary = GlassLightAccentSecondary,
                cardCornerRadius = 20.dp,
                borderWidth = 1.dp,
                isGlass = true,
                shadowElevation = 8.dp,
                invertedFill = GlassLightAccent,
                invertedText = Color.White,
                isDark = false
            )
        }
    }

    val animDuration = 250
    val animatedBackground by androidx.compose.animation.animateColorAsState(shredColors.background, androidx.compose.animation.core.tween(animDuration), label = "bg")
    val animatedSurface by androidx.compose.animation.animateColorAsState(shredColors.surface, androidx.compose.animation.core.tween(animDuration), label = "surf")
    val animatedCard by androidx.compose.animation.animateColorAsState(shredColors.cardBackground, androidx.compose.animation.core.tween(animDuration), label = "card")
    val animatedBorder by androidx.compose.animation.animateColorAsState(shredColors.border, androidx.compose.animation.core.tween(animDuration), label = "border")
    val animatedBorderSubtle by androidx.compose.animation.animateColorAsState(shredColors.borderSubtle, androidx.compose.animation.core.tween(animDuration), label = "borderSubtle")
    val animatedTextPrimary by androidx.compose.animation.animateColorAsState(shredColors.textPrimary, androidx.compose.animation.core.tween(animDuration), label = "textPrim")
    val animatedTextSecondary by androidx.compose.animation.animateColorAsState(shredColors.textSecondary, androidx.compose.animation.core.tween(animDuration), label = "textSec")
    val animatedAccent by androidx.compose.animation.animateColorAsState(shredColors.accent, androidx.compose.animation.core.tween(animDuration), label = "accent")
    val animatedAccentSecondary by androidx.compose.animation.animateColorAsState(shredColors.accentSecondary, androidx.compose.animation.core.tween(animDuration), label = "accentSec")
    val animatedInvertedFill by androidx.compose.animation.animateColorAsState(shredColors.invertedFill, androidx.compose.animation.core.tween(animDuration), label = "invFill")
    val animatedInvertedText by androidx.compose.animation.animateColorAsState(shredColors.invertedText, androidx.compose.animation.core.tween(animDuration), label = "invText")
    val animatedCornerRadius by androidx.compose.animation.core.animateDpAsState(shredColors.cardCornerRadius, androidx.compose.animation.core.tween(animDuration), label = "radius")
    val animatedBorderWidth by androidx.compose.animation.core.animateDpAsState(shredColors.borderWidth, androidx.compose.animation.core.tween(animDuration), label = "borderW")
    val animatedShadowElevation by androidx.compose.animation.core.animateDpAsState(shredColors.shadowElevation, androidx.compose.animation.core.tween(animDuration), label = "shadow")

    val finalColors = shredColors.copy(
        background = animatedBackground,
        surface = animatedSurface,
        cardBackground = animatedCard,
        border = animatedBorder,
        borderSubtle = animatedBorderSubtle,
        textPrimary = animatedTextPrimary,
        textSecondary = animatedTextSecondary,
        accent = animatedAccent,
        accentSecondary = animatedAccentSecondary,
        invertedFill = animatedInvertedFill,
        invertedText = animatedInvertedText,
        cardCornerRadius = animatedCornerRadius,
        borderWidth = animatedBorderWidth,
        shadowElevation = animatedShadowElevation
    )

    val materialColors = if (isDark) {
        darkColorScheme(
            background = finalColors.background,
            surface = finalColors.surface,
            primary = finalColors.accent,
            onBackground = finalColors.textPrimary,
            onSurface = finalColors.textPrimary
        )
    } else {
        lightColorScheme(
            background = finalColors.background,
            surface = finalColors.surface,
            primary = finalColors.accent,
            onBackground = finalColors.textPrimary,
            onSurface = finalColors.textPrimary
        )
    }

    val typography = when (themeMode) {
        ThemeMode.BRUTALISM -> BrutalTypography
        ThemeMode.MINIMALISM -> MinimalTypography
        ThemeMode.GLASSMORPHISM -> GlassTypography
    }

    val animSpec = when (themeMode) {
        ThemeMode.BRUTALISM -> BrutalAnimationSpec
        ThemeMode.MINIMALISM -> MinimalAnimationSpec
        ThemeMode.GLASSMORPHISM -> GlassAnimationSpec
    }

    val shapes = when (themeMode) {
        ThemeMode.BRUTALISM -> BrutalShapes
        ThemeMode.MINIMALISM -> MinimalShapes
        ThemeMode.GLASSMORPHISM -> GlassShapes
    }

    CompositionLocalProvider(
        LocalShredColors provides finalColors,
        LocalThemeMode provides themeMode,
        LocalColorMode provides colorMode,
        LocalShredTypography provides typography,
        LocalShredAnimation provides animSpec
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = typography,
            shapes = shapes,
            content = {
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = shredColors.isGlass,
                        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)),
                        exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(250))
                    ) {
                        com.shred.app.ui.components.AnimatedGradientBackground(isDarkMode = isDark) {}
                    }
                    content()
                }
            }
        )
    }
}
