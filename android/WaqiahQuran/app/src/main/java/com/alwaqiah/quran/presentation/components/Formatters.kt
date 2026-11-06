package com.alwaqiah.quran.presentation.components

import com.alwaqiah.quran.core.util.toPersianDigits
import java.util.Locale

/** 125000 → "۲:۰۵" */
fun formatDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds).toPersianDigits()
}

/** 12_345_678 → "۱۱٫۸ مگابایت" */
fun formatBytes(bytes: Long): String {
    if (bytes < 1024 * 1024) return String.format(Locale.US, "%.0f", bytes / 1024.0).toPersianDigits() + " کیلوبایت"
    return String.format(Locale.US, "%.1f", bytes / (1024.0 * 1024.0)).replace('.', '٫').toPersianDigits() + " مگابایت"
}

fun formatSpeed(speed: Float): String =
    String.format(Locale.US, "%.2f", speed).trimEnd('0').trimEnd('.').replace('.', '٫').toPersianDigits() + "×"
