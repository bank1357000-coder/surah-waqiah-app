package com.alwaqiah.quran.domain.memorization

import org.junit.Assert.assertEquals
import org.junit.Test

class MemorizationStatsTest {
    private fun p(v: Int, s: MemorizationStatus) = v to MemorizationProgress(v, 1, s, 0)

    @Test fun summarizesStatuses() {
        val summary = MemorizationStats.summarize(
            mapOf(p(1, MemorizationStatus.MEMORIZED), p(2, MemorizationStatus.MEMORIZED), p(3, MemorizationStatus.NEEDS_REVISION), p(4, MemorizationStatus.LEARNING)),
            total = 96,
        )
        assertEquals(2, summary.memorized)
        assertEquals(1, summary.needsRevision)
        assertEquals(1, summary.learning)
        assertEquals(2f / 96, summary.fraction, 0.0001f)
    }

    @Test fun goalFractionIsClamped() {
        assertEquals(0.5f, MemorizationStats.goalFraction(DailyActivity("d", versesPracticed = 2), 4), 0.0001f)
        assertEquals(1f, MemorizationStats.goalFraction(DailyActivity("d", versesPracticed = 9), 4), 0.0001f)
        assertEquals(0f, MemorizationStats.goalFraction(null, 4), 0.0001f)
    }

    @Test fun repetitionPromotesNewToLearningButNeverDowngrades() {
        assertEquals(MemorizationStatus.LEARNING, MemorizationStats.statusAfterRepetition(null))
        assertEquals(MemorizationStatus.LEARNING, MemorizationStats.statusAfterRepetition(MemorizationStatus.NEW))
        assertEquals(MemorizationStatus.MEMORIZED, MemorizationStats.statusAfterRepetition(MemorizationStatus.MEMORIZED))
        assertEquals(MemorizationStatus.NEEDS_REVISION, MemorizationStats.statusAfterRepetition(MemorizationStatus.NEEDS_REVISION))
    }
}
