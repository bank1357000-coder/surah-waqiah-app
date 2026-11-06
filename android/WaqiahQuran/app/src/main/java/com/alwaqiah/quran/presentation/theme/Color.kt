package com.alwaqiah.quran.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColorScheme(
    primary = Color(0xFF0E5E4E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE9DF),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFFA67C2E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5E3BC),
    onSecondaryContainer = Color(0xFF2B1D00),
    tertiary = Color(0xFF7A4E2D),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F4EA),
    onBackground = Color(0xFF1C1B17),
    surface = Color(0xFFFFFBF2),
    onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFEDE6D6),
    onSurfaceVariant = Color(0xFF4D4639),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF6EC),
    surfaceContainer = Color(0xFFF3EDDF),
    surfaceContainerHigh = Color(0xFFEDE6D6),
    surfaceContainerHighest = Color(0xFFE6DFCE),
    outline = Color(0xFFB9AE96),
    outlineVariant = Color(0xFFDCD3BF),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD3BC),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF0B4A3E),
    onPrimaryContainer = Color(0xFFCFE9DF),
    secondary = Color(0xFFE2C07A),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5A4314),
    onSecondaryContainer = Color(0xFFF5E3BC),
    tertiary = Color(0xFFE8B98F),
    onTertiary = Color(0xFF452B12),
    background = Color(0xFF0C1412),
    onBackground = Color(0xFFE6E2D9),
    surface = Color(0xFF111B18),
    onSurface = Color(0xFFE6E2D9),
    surfaceVariant = Color(0xFF26302C),
    onSurfaceVariant = Color(0xFFC4C0B4),
    surfaceContainerLowest = Color(0xFF09100E),
    surfaceContainerLow = Color(0xFF131D1A),
    surfaceContainer = Color(0xFF16211E),
    surfaceContainerHigh = Color(0xFF1C2925),
    surfaceContainerHighest = Color(0xFF23312D),
    outline = Color(0xFF5E6A64),
    outlineVariant = Color(0xFF34403B),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

/** Brand colours outside the Material scheme (hero card, ornaments, verse highlight). */
@Immutable
data class WaqiahExtraColors(
    val heroStart: Color,
    val heroEnd: Color,
    val onHero: Color,
    val gold: Color,
    val verseHighlight: Color,
)

internal val LightExtraColors = WaqiahExtraColors(
    heroStart = Color(0xFF0A3D33),
    heroEnd = Color(0xFF14705C),
    onHero = Color(0xFFFDF8EC),
    gold = Color(0xFFC79A45),
    verseHighlight = Color(0x1FC79A45),
)

internal val DarkExtraColors = WaqiahExtraColors(
    heroStart = Color(0xFF082B24),
    heroEnd = Color(0xFF0F4F42),
    onHero = Color(0xFFF3EEE2),
    gold = Color(0xFFD9B25F),
    verseHighlight = Color(0x26D9B25F),
)

val LocalWaqiahColors = staticCompositionLocalOf { LightExtraColors }
