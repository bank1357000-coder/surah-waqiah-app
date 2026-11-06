package com.alwaqiah.quran.domain.playback

import com.alwaqiah.quran.domain.playback.RepeatSettings.Companion.UNLIMITED

/**
 * Pure, single-threaded repetition state machine (no Android dependencies, fully unit-testable).
 *
 * Contract:
 *  - [start] / [onManualSeek] set the current verse and reset counters.
 *  - [onVerseCompleted] is called exactly when the current verse's end timestamp is reached.
 *    Events for any other verse are ignored (returns null), which protects against duplicate or
 *    stale callbacks after seeks.
 *  - Every decision is finite except when the user explicitly selected [UNLIMITED].
 */
class RepeatEngine(private val lastVerse: Int = RepeatSettings.LAST_VERSE) {

    var settings: RepeatSettings = RepeatSettings().normalized(lastVerse)
        private set
    var currentVerse: Int = 1
        private set
    /** Completed plays of the current verse in this cycle. */
    var versePlays: Int = 0
        private set
    /** Completed plays of the active range. */
    var rangePlays: Int = 0
        private set

    private var activeStart = 1
    private var activeEnd = lastVerse
    val activeRange: IntRange get() = activeStart..activeEnd

    fun configure(newSettings: RepeatSettings, current: Int) {
        settings = newSettings.normalized(lastVerse)
        start(current)
    }

    /** Prepares a fresh cycle and returns the verse playback must start from. */
    fun start(requested: Int): Int {
        val verse = requested.coerceIn(1, lastVerse)
        versePlays = 0
        rangePlays = 0
        when (settings.scope) {
            RepeatScope.VERSE -> { activeStart = verse; activeEnd = verse }
            RepeatScope.RANGE -> { activeStart = settings.rangeStart; activeEnd = settings.rangeEnd }
            RepeatScope.SURAH -> { activeStart = 1; activeEnd = lastVerse }
        }
        currentVerse = if (verse in activeStart..activeEnd) verse else activeStart
        return currentVerse
    }

    /** User moved to another verse (next/previous, seek bar, notification). */
    fun onManualSeek(verse: Int) {
        currentVerse = verse.coerceIn(1, lastVerse)
        versePlays = 0
        if (settings.scope == RepeatScope.VERSE) {
            activeStart = currentVerse
            activeEnd = currentVerse
            rangePlays = 0
        }
    }

    fun onVerseCompleted(verse: Int): RepeatDecision? {
        if (verse != currentVerse) return null

        // Outside the active range (user jumped away): approach the range or stop after it.
        if (verse < activeStart) return advanceTo(verse + 1)
        if (verse > activeEnd) return RepeatDecision.Finish

        versePlays++
        if (settings.verseRepeatCount == UNLIMITED || versePlays < settings.verseRepeatCount) {
            return RepeatDecision.Restart(verse, settings.pauseBetweenRepeatsMs)
        }
        versePlays = 0

        if (verse < activeEnd) return advanceTo(verse + 1)

        rangePlays++
        if (settings.rangeRepeatCount == UNLIMITED || rangePlays < settings.rangeRepeatCount) {
            currentVerse = activeStart
            return RepeatDecision.Restart(activeStart, settings.pauseBetweenRepeatsMs)
        }
        rangePlays = 0

        if (settings.continueToNextRange && activeEnd < lastVerse) {
            val length = activeEnd - activeStart + 1
            activeStart = activeEnd + 1
            activeEnd = minOf(lastVerse, activeStart + length - 1)
            return advanceTo(activeStart)
        }
        return RepeatDecision.Finish
    }

    private fun advanceTo(verse: Int): RepeatDecision {
        if (verse > lastVerse) return RepeatDecision.Finish
        currentVerse = verse
        return RepeatDecision.Advance(verse)
    }
}
