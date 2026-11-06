package com.alwaqiah.quran.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.presentation.theme.WaqiahThemeExtras
import kotlin.math.cos
import kotlin.math.sin

/** Draws an eight-pointed star (Rub el Hizb) made of two overlapping squares. */
fun DrawScope.drawEightPointStar(
    center: Offset,
    radius: Float,
    color: Color,
    style: DrawStyle = Fill,
) {
    for (rotation in floatArrayOf(0f, 45f)) {
        val path = Path()
        for (i in 0 until 4) {
            val angle = Math.toRadians((rotation + 45f + i * 90f).toDouble())
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color, style = style)
    }
}

/** Ornamental verse-number medallion. */
@Composable
fun VerseNumberBadge(number: Int, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val gold = WaqiahThemeExtras.colors.gold
    Box(modifier.size(size).testTag("verse-number-$number"), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            drawEightPointStar(center, this.size.minDimension / 2f, gold.copy(alpha = 0.18f))
            drawEightPointStar(center, this.size.minDimension / 2f - 1.dp.toPx(), gold, Stroke(1.2.dp.toPx()))
        }
        Text(
            text = number.toPersianDigits(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
