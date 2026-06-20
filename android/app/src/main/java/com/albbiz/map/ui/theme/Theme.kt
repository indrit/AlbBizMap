// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── TIER BADGE COLORS (Premium/Featured/Sponsored) ─────────────────────
val TierBronze = Color(0xFFCD7F32)   // Premium
val TierSilver = Color(0xFFC0C0C0)   // Featured
val TierGold = Color(0xFFC7BF3F)     // Sponsored

// ── LIGHT COLOR SCHEME ────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE41E20),           // MeTont Red
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE41E20),  // Red for TopAppBar
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF757575),         // Grey
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF1A1A1A),

    tertiary = Color(0xFFFFAA00),          // Gold for premium
    onTertiary = Color.White,

    background = Color(0xFFFFF8F0),      // Off white background
    onBackground = Color(0xFF1A1A1A),

    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),

    error = Color(0xFFB00020),
    onError = Color.White,

    outline = Color(0xFFE0E0E0)
)

// ── DARK COLOR SCHEME ─────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5252),           // Lighter red for dark mode
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC41A1C),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFFBDBDBD),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF2C2C2C),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFFFCC02),
    onTertiary = Color(0xFF1A1A1A),

    background = Color(0xFF121212),
    onBackground = Color(0xFF1A1A1A),

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),

    error = Color(0xFFCF6679),
    onError = Color.White,

    outline = Color(0xFF3C3C3C)
)

@Composable
fun AlbBizMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MeTontTypography,
        content = content
    )
}