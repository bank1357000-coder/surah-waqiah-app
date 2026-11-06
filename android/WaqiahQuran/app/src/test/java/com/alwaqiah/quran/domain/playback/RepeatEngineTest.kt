package com.alwaqiah.quran.domain.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepeatEngineTest {

    private fun engine(settings: RepeatSettings, start: Int, last: Int = 96) =
        RepeatEngine(last).apply { configure(settings, start); start(start) }

    @Test fun surahPlainAdvancesThenFinishes() {
        val e = engine(RepeatSettings(), 95)
        assertEquals(RepeatDecision.Advance(96), e.onVerseCompleted(95))
        assertEquals(RepeatDecision.Finish, e.onVerseCompleted(96))
    }

    @Test fun verseRepeatedThreeTimesThenFinishes() {
        val e = engine(RepeatSettings(scope = RepeatScope.VERSE, verseRepeatCount = 3, pauseBetweenRepeatsMs = 500), 10)
        assertEquals(RepeatDecision.Restart(10, 500), e.onVerseCompleted(10))
        assertEquals(RepeatDecision.Restart(10, 500), e.onVerseCompleted(10))
        assertEquals(RepeatDecision.Finish, e.onVerseCompleted(10))
    }

    @Test fun verseRepeatThenContinueToNextVerse() {
        val e = engine(RepeatSettings(scope = RepeatScope.VERSE, verseRepeatCount = 2, continueToNextRange = true), 5)
        assertEquals(RepeatDecision.Restart(5, 0), e.onVerseCompleted(5))
        assertEquals(RepeatDecision.Advance(6), e.onVerseCompleted(5))
        assertEquals(6..6, e.activeRange)
    }

    @Test fun unlimitedNeverFinishesButStaysOnVerse() {
        val e = engine(RepeatSettings(scope = RepeatScope.VERSE, verseRepeatCount = RepeatSettings.UNLIMITED), 7)
        repeat(1_000) { assertEquals(RepeatDecision.Restart(7, 0), e.onVerseCompleted(7)) }
        assertEquals(7, e.currentVerse)
    }

    @Test fun rangeRepeatsTwiceThenFinishes() {
        val e = engine(RepeatSettings(scope = RepeatScope.RANGE, rangeStart = 3, rangeEnd = 4, rangeRepeatCount = 2), 3)
        assertEquals(RepeatDecision.Advance(4), e.onVerseCompleted(3))
        assertEquals(RepeatDecision.Restart(3, 0), e.onVerseCompleted(4))
        assertEquals(RepeatDecision.Advance(4), e.onVerseCompleted(3))
        assertEquals(RepeatDecision.Finish, e.onVerseCompleted(4))
    }

    @Test fun rangeContinuesToNextBlockOfSameLength() {
        val e = engine(RepeatSettings(scope = RepeatScope.RANGE, rangeStart = 1, rangeEnd = 3, continueToNextRange = true), 1)
        e.onVerseCompleted(1); e.onVerseCompleted(2)
        assertEquals(RepeatDecision.Advance(4), e.onVerseCompleted(3))
        assertEquals(4..6, e.activeRange)
    }

    @Test fun lastBlockIsClampedToSurahEnd() {
        val e = engine(RepeatSettings(scope = RepeatScope.RANGE, rangeStart = 94, rangeEnd = 95, continueToNextRange = true), 94)
        e.onVerseCompleted(94)
        assertEquals(RepeatDecision.Advance(96), e.onVerseCompleted(95))
        assertEquals(96..96, e.activeRange)
        assertEquals(RepeatDecision.Finish, e.onVerseCompleted(96))
    }

    @Test fun staleOrDuplicateEventsAreIgnored() {
        val e = engine(RepeatSettings(), 10)
        assertNull(e.onVerseCompleted(9))
        assertEquals(RepeatDecision.Advance(11), e.onVerseCompleted(10))
        assertNull(e.onVerseCompleted(10)) // duplicate callback for the old verse
    }

    @Test fun startOutsideRangeSnapsToRangeStart() {
        val e = RepeatEngine().apply { configure(RepeatSettings(scope = RepeatScope.RANGE, rangeStart = 20, rangeEnd = 25), 1) }
        assertEquals(20, e.start(50))
    }

    @Test fun manualSeekResetsVerseCounter() {
        val e = engine(RepeatSettings(scope = RepeatScope.SURAH, verseRepeatCount = 3), 1)
        e.onVerseCompleted(1)
        assertEquals(1, e.versePlays)
        e.onManualSeek(30)
        assertEquals(0, e.versePlays)
        assertEquals(RepeatDecision.Restart(30, 0), e.onVerseCompleted(30))
    }

    @Test fun normalizationSwapsAndClamps() {
        val s = RepeatSettings(rangeStart = 120, rangeEnd = -3, verseRepeatCount = -1, pauseBetweenRepeatsMs = 99_000).normalized()
        assertEquals(1, s.rangeStart)
        assertEquals(96, s.rangeEnd)
        assertEquals(0, s.verseRepeatCount)
        assertTrue(s.pauseBetweenRepeatsMs <= RepeatSettings.MAX_PAUSE_MS)
    }
}
