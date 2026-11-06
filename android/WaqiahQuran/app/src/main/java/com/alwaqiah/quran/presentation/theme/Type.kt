package com.alwaqiah.quran.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.alwaqiah.quran.R
import com.alwaqiah.quran.domain.model.ArabicScript

/** Persian UI font (SIL OFL 1.1). */
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

object QuranFonts {
    /** KFGQPC Uthmanic Script HAFS (Uthman Taha calligraphy), paired with the KFGQPC Hafs text. */
    val Hafs = FontFamily(Font(R.font.uthmanic_hafs))

    /** Amiri Quran (SIL OFL 1.1), paired with the Tanzil Uthmani text. */
    val Amiri = FontFamily(Font(R.font.amiri_quran))
}

fun ArabicScript.fontFamily(): FontFamily = when (this) {
    ArabicScript.HAFS_KFGQPC -> QuranFonts.Hafs
    ArabicScript.TANZIL_AMIRI -> QuranFonts.Amiri
}

private val base = Typography()

private fun TextStyle.vazir() = copy(fontFamily = Vazirmatn)

val WaqiahTypography = Typography(
    displayLarge = base.displayLarge.vazir(),
    displayMedium = base.displayMedium.vazir(),
    displaySmall = base.displaySmall.vazir(),
    headlineLarge = base.headlineLarge.vazir().copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.vazir().copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.vazir().copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.vazir().copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.vazir().copy(fontWeight = FontWeight.Medium),
    titleSmall = base.titleSmall.vazir().copy(fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.vazir(),
    bodyMedium = base.bodyMedium.vazir(),
    bodySmall = base.bodySmall.vazir(),
    labelLarge = base.labelLarge.vazir().copy(fontWeight = FontWeight.Medium),
    labelMedium = base.labelMedium.vazir().copy(fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.vazir(),
)
