package com.shred.app.ui.theme

import androidx.compose.ui.graphics.Color

// Shared Action Overlay Colors
val SwipeDeleteRed = Color(0xFFFF2A2A)
val SwipeKeepGreen = Color(0xFF00E676)
val SwipeFavoriteGold = Color(0xFFFFD700)

// 1. Brutalism Colors (Stark, High Contrast, Raw Monochrome & Inverted Fills)
// Dark Mode
val BrutalDarkBackground = Color(0xFF050505)
val BrutalDarkSurface = Color(0xFF121212)
val BrutalDarkCard = Color(0xFF1A1A1A)
val BrutalDarkBorder = Color(0xFFFFFFFF)
val BrutalDarkBorderSubtle = Color(0xFF333333)
val BrutalDarkTextPrimary = Color(0xFFFFFFFF)
val BrutalDarkTextSecondary = Color(0xFF888888)
val BrutalDarkAccent = Color(0xFFFFE600) // Raw Electric Yellow
val BrutalDarkInvertedFill = Color(0xFFFFFFFF)
val BrutalDarkInvertedText = Color(0xFF000000)

// Light Mode
val BrutalLightBackground = Color(0xFFF4F4F0) // Raw chalk white
val BrutalLightSurface = Color(0xFFFFFFFF)
val BrutalLightCard = Color(0xFFEBEBE6)
val BrutalLightBorder = Color(0xFF000000)
val BrutalLightBorderSubtle = Color(0xFFCCCCCC)
val BrutalLightTextPrimary = Color(0xFF000000)
val BrutalLightTextSecondary = Color(0xFF555555)
val BrutalLightAccent = Color(0xFFFFCC00) // Electric Yellow/Amber
val BrutalLightInvertedFill = Color(0xFF000000)
val BrutalLightInvertedText = Color(0xFFFFFFFF)

// Backward-compatible Brutalism aliases
val BrutalBackground = BrutalDarkBackground
val BrutalSurface = BrutalDarkSurface
val BrutalCard = BrutalDarkCard
val BrutalBorder = BrutalDarkBorder
val BrutalBorderSubtle = BrutalDarkBorderSubtle
val BrutalTextPrimary = BrutalDarkTextPrimary
val BrutalTextSecondary = BrutalDarkTextSecondary
val BrutalAccent = BrutalDarkAccent

// 2. Minimalism Colors (Quiet Luxury / Editorial Obsidian: Warm Alabaster, Sandstone, Champagne Accent)
// Dark Mode
val MinimalDarkBackground = Color(0xFF0F1115) // Deep Charcoal Slate
val MinimalDarkSurface = Color(0xFF16181E)
val MinimalDarkCard = Color(0xFF1E222B) // Elevated Card Surface
val MinimalDarkBorder = Color.Transparent // Zero Borders
val MinimalDarkBorderSubtle = Color(0x1F9E988D) // Subtle hairline
val MinimalDarkTextPrimary = Color(0xFFF8F6F0) // Warm Alabaster
val MinimalDarkTextSecondary = Color(0xFF9E988D) // Sandstone Gray
val MinimalDarkAccent = Color(0xFFD4AF37) // Subtle Champagne Gold

// Light Mode
val MinimalLightBackground = Color(0xFFFAFAF7) // Warm Alabaster Off-white
val MinimalLightSurface = Color(0xFFFFFFFF)
val MinimalLightCard = Color(0xFFFFFFFF)
val MinimalLightBorder = Color.Transparent // Zero Borders
val MinimalLightBorderSubtle = Color(0x12000000)
val MinimalLightTextPrimary = Color(0xFF18191B) // Deep charcoal
val MinimalLightTextSecondary = Color(0xFF7A7870) // Muted stone
val MinimalLightAccent = Color(0xFFB8860B) // Refined Amber Gold

// Backward-compatible Minimalism aliases
val MinimalBackground = MinimalDarkBackground
val MinimalSurface = MinimalDarkSurface
val MinimalCard = MinimalDarkCard
val MinimalBorder = MinimalDarkBorder
val MinimalBorderSubtle = MinimalDarkBorderSubtle
val MinimalTextPrimary = MinimalDarkTextPrimary
val MinimalTextSecondary = MinimalDarkTextSecondary
val MinimalAccent = MinimalDarkAccent

// 3. Glassmorphism Colors (Cosmic Space / Luminous Ethereal, Frosted Glass & Neon Glowing Rims)
// Dark Mode
val GlassDarkBackground = Color(0xFF030408)
val GlassDarkSurface = Color(0x24FFFFFF)
val GlassDarkCard = Color(0x18FFFFFF)
val GlassDarkBorder = Color(0x6600F2FE) // Neon Cyan Glow Rim
val GlassDarkBorderSubtle = Color(0x33FF007F) // Neon Magenta Secondary Glow Rim
val GlassDarkTextPrimary = Color(0xFFFFFFFF)
val GlassDarkTextSecondary = Color(0xCCFFFFFF)
val GlassDarkAccent = Color(0xFF00F2FE) // Vibrant Cyan Glow
val GlassDarkAccentSecondary = Color(0xFFFF007F) // Vibrant Neon Magenta

// Light Mode
val GlassLightBackground = Color(0xFFEEF2F6) // Pale ethereal lavender/blue
val GlassLightSurface = Color(0x70FFFFFF) // Translucent frosted white
val GlassLightCard = Color(0x50FFFFFF)
val GlassLightBorder = Color(0x8000B4D8) // Cyan glass rim
val GlassLightBorderSubtle = Color(0x40E056FD) // Light magenta glow rim
val GlassLightTextPrimary = Color(0xFF0F172A) // Deep slate
val GlassLightTextSecondary = Color(0xFF475569) // Slate secondary
val GlassLightAccent = Color(0xFF0284C7) // Sky blue
val GlassLightAccentSecondary = Color(0xFFD946EF) // Bright fuchsia

// Backward-compatible Glassmorphism aliases
val GlassBackground = GlassDarkBackground
val GlassSurface = GlassDarkSurface
val GlassCard = GlassDarkCard
val GlassBorder = GlassDarkBorder
val GlassBorderSubtle = GlassDarkBorderSubtle
val GlassTextPrimary = GlassDarkTextPrimary
val GlassTextSecondary = GlassDarkTextSecondary
val GlassAccent = GlassDarkAccent
val GlassMagenta = GlassDarkAccentSecondary
