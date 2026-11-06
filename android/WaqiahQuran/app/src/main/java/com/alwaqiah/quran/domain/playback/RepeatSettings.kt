package com.alwaqiah.quran.domain.playback

/** What the repeat engine loops over. */
enum class RepeatScope { VERSE, RANGE, SURAH }

/**
 * User-facing repetition configuration. Counts use [UNLIMITED] (0) for endless repetition.
 *
 * - [verseRepeatCount]: how many times every verse is played before moving on.
 * - [rangeRepeatCount]: how many times the active range (or whole surah) is played.
 * - [continueToNextRange]: after finishing, move on to the next block of the same length
 *   (VERSE scope → next verse; RANGE scope → next range), instead of stopping.
 */
data class RepeatSettings(
    val scope: RepeatScope = RepeatScope.SURAH,
    val verseRepeatCount: Int = 1,
    val rangeStart: Int = 1,
    val rangeEnd: Int = LAST_VERSE,
    val rangeRepeatCount: Int = 1,
    val pauseBetweenRepeatsMs: Long = 0L,
    val continueToNextRange: Boolean = false,
) {
    fun normalized(lastVerse: Int = LAST_VERSE): RepeatSettings {
        val start = rangeStart.coerceIn(1, lastVerse)
        val end = rangeEnd.coerceIn(1, lastVerse)
        return copy(
            verseRepeatCount = verseRepeatCount.coerceIn(UNLIMITED, MAX_COUNT),
            rangeRepeatCount = rangeRepeatCount.coerceIn(UNLIMITED, MAX_COUNT),
            rangeStart = minOf(start, end),
            rangeEnd = maxOf(start, end),
            pauseBetweenRepeatsMs = pauseBetweenRepeatsMs.coerceIn(0L, MAX_PAUSE_MS),
        )
    }

    companion object {
        const val UNLIMITED = 0
        const val MAX_COUNT = 999
        const val MAX_PAUSE_MS = 10_000L
        const val LAST_VERSE = 96
    }
}

/** Instruction produced by [RepeatEngine] when a verse finishes. */
sealed interface RepeatDecision {
    /** Contiguous continuation: audio keeps flowing, no seek needed. */
    data class Advance(val verse: Int) : RepeatDecision

    /** Seek to the start of [verse] (after an optional pause) — used for repeats and range jumps. */
    data class Restart(val verse: Int, val pauseMs: Long) : RepeatDecision

    data object Finish : RepeatDecision
}
