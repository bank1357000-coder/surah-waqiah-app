package com.alwaqiah.quran.domain.model

import com.alwaqiah.quran.domain.playback.RepeatScope
import com.alwaqiah.quran.domain.playback.RepeatSettings

data class Bookmark(
    val id: Long,
    val verseNumber: Int,
    val createdAt: Long,
    val note: String?,
)

data class ReadingPosition(
    val verseNumber: Int,
    val visitedAt: Long,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class ArabicScript { HAFS_KFGQPC, TANZIL_AMIRI }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val arabicFontSizeSp: Float = 28f,
    val arabicScript: ArabicScript = ArabicScript.HAFS_KFGQPC,
    val primaryTranslationId: String = "fa.makarem",
    val showEnglishTranslation: Boolean = false,
    val englishTranslationId: String = "en.sahih",
    val reciterId: Int = 7,
    val playbackSpeed: Float = 1.0f,
    val speedStep: Float = 0.25f,
    /** 0 means unlimited repetition. */
    val repeatCount: Int = 1,
    val pauseBetweenRepeatsMs: Long = 0L,
    val autoScroll: Boolean = true,
    val hapticFeedback: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val downloadOnWifiOnly: Boolean = true,
    /** Arabic line height as a multiple of the font size. */
    val arabicLineSpacing: Float = 1.9f,
    val repeatScope: RepeatScope = RepeatScope.SURAH,
    val rangeStart: Int = 1,
    val rangeEnd: Int = RepeatSettings.LAST_VERSE,
    /** 0 means unlimited. */
    val rangeRepeatCount: Int = 1,
    val continueToNextRange: Boolean = false,
    /** Restored after process death so playback resumes at the right verse. */
    val lastPlayedVerse: Int = 1,
) {
    /** [repeatCount] is the per-verse count; the rest describe the range/surah loop. */
    fun toRepeatSettings() = RepeatSettings(
        scope = repeatScope,
        verseRepeatCount = repeatCount,
        rangeStart = rangeStart,
        rangeEnd = rangeEnd,
        rangeRepeatCount = rangeRepeatCount,
        pauseBetweenRepeatsMs = pauseBetweenRepeatsMs,
        continueToNextRange = continueToNextRange,
    ).normalized()

    companion object {
        const val MIN_FONT_SIZE = 18f
        const val MAX_FONT_SIZE = 48f
        const val MIN_SPEED = 1.0f
        const val MAX_SPEED = 2.0f
        val SPEED_STEPS = listOf(0.05f, 0.1f, 0.25f)
        const val MAX_PAUSE_MS = 10_000L
    }
}
