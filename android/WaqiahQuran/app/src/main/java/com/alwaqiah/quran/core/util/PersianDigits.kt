package com.alwaqiah.quran.core.util

private const val PERSIAN_ZERO = '\u06F0'

/** Converts ASCII digits to Persian (Extended Arabic-Indic) digits. */
fun String.toPersianDigits(): String = buildString(length) {
    for (c in this@toPersianDigits) {
        append(if (c in '0'..'9') PERSIAN_ZERO + (c - '0') else c)
    }
}

fun Int.toPersianDigits(): String = toString().toPersianDigits()
