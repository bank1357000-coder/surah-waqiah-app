package com.alwaqiah.quran.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import com.alwaqiah.quran.domain.model.ThemeMode

private val WaqiahShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Brand palette is fixed (no dynamic colour) to preserve the Islamic visual identity. */
@Composable
fun WaqiahTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalWaqiahColors provides if (dark) DarkExtraColors else LightExtraColors) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = WaqiahTypography,
            shapes = WaqiahShapes,
            content = content,
        )
    }
}

object WaqiahThemeExtras {
    val colors: WaqiahExtraColors
        @Composable get() = LocalWaqiahColors.current
}
