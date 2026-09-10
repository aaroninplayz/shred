package com.shred.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.shred.app.data.model.ThemeMode

data class ShredThemeColors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color
)

val LocalShredColors = staticCompositionLocalOf {
    ShredThemeColors(
        background = MonoBackground,
        surface = MonoSurface,
        cardBackground = MonoCard,
        border = MonoBorder,
        borderSubtle = MonoBorderSubtle,
        textPrimary = MonoTextPrimary,
        textSecondary = MonoTextSecondary,
        accent = MonoAccent
    )
}

@Composable
fun ShredTheme(
    themeMode: ThemeMode = ThemeMode.MONOCHROME,
    content: @Composable () -> Unit
) {
    val shredColors = when (themeMode) {
        ThemeMode.MONOCHROME -> ShredThemeColors(
            background = MonoBackground,
            surface = MonoSurface,
            cardBackground = MonoCard,
            border = MonoBorder,
            borderSubtle = MonoBorderSubtle,
            textPrimary = MonoTextPrimary,
            textSecondary = MonoTextSecondary,
            accent = MonoAccent
        )
        ThemeMode.NEON -> ShredThemeColors(
            background = NeonBackground,
            surface = NeonSurface,
            cardBackground = NeonCard,
            border = NeonBorder,
            borderSubtle = NeonBorderSubtle,
            textPrimary = NeonTextPrimary,
            textSecondary = NeonTextSecondary,
            accent = NeonAccent
        )
        ThemeMode.NEO_BRUTALISM -> ShredThemeColors(
            background = NeoBackground,
            surface = NeoSurface,
            cardBackground = NeoCard,
            border = NeoBorder,
            borderSubtle = NeoBorderSubtle,
            textPrimary = NeoTextPrimary,
            textSecondary = NeoTextSecondary,
            accent = NeoAccent
        )
    }

    val materialColors = darkColorScheme(
        background = shredColors.background,
        surface = shredColors.surface,
        primary = shredColors.accent,
        onBackground = shredColors.textPrimary,
        onSurface = shredColors.textPrimary
    )

    val shapes = if (themeMode == ThemeMode.NEO_BRUTALISM) BrutalShapesNeo else BrutalShapesMono

    CompositionLocalProvider(LocalShredColors provides shredColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = BrutalTypography,
            shapes = shapes,
            content = content
        )
    }
}
