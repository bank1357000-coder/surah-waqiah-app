package com.alwaqiah.quran.domain.playback

import com.alwaqiah.quran.domain.model.VerseTiming
import com.alwaqiah.quran.domain.usecase.FindVerseAtPositionUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Fake audio source: a virtual clock walking through verse timestamps the way ExoPlayer would,
 * delivering "boundary reached" events and applying the engine's decisions (seek / continue / stop).
 * Verifies the exact verse sequence heard by the user, with no real player involved.
 */
class PlaybackSimulatorTest {

    private val timings = (1..10).map { VerseTiming(it, (it - 1) * 1_000L, it * 1_000L) }
    private val find = FindVerseAtPositionUseCase()

    private fun simulate(settings: RepeatSettings, startVerse: Int, maxEvents: Int = 200): List<Int> {
        val engine = RepeatEngine(lastVerse = 10).apply { configure(settings, startVerse) }
        var verse = engine.start(startVerse)
        var position = timings[verse - 1].fromMs
        val heard = mutableListOf<Int>()
        repeat(maxEvents) {
            heard += find(timings, position)!!
            position = timings[verse - 1].toMs // play until the boundary
            when (val d = engine.onVerseCompleted(verse)) {
                is RepeatDecision.Advance -> verse = d.verse            // no seek: position already at next start
                is RepeatDecision.Restart -> { verse = d.verse; position = timings[verse - 1].fromMs }
                RepeatDecision.Finish, null -> return heard
            }
        }
        return heard
    }

    @Test fun wholeSurahOnce() = assertEquals((1..10).toList(), simulate(RepeatSettings(), 1))

    @Test fun eachVerseTwiceInRange() = assertEquals(
        listOf(4, 4, 5, 5, 6, 6),
        simulate(RepeatSettings(scope = RepeatScope.RANGE, rangeStart = 4, rangeEnd = 6, verseRepeatCount = 2), 4),
    )

    @Test fun rangeTwiceThenNextRange() = assertEquals(
        listOf(1, 2, 1, 2, 3, 4, 3, 4, 5, 6, 5, 6, 7, 8, 7, 8, 9, 10, 9, 10),
        simulate(
            RepeatSettings(scope = RepeatScope.RANGE, rangeStart = 1, rangeEnd = 2, rangeRepeatCount = 2, continueToNextRange = true),
            1,
        ),
    )

    @Test fun surahRepeatedTwice() = assertEquals(
        (1..10).toList() + (1..10).toList(),
        simulate(RepeatSettings(rangeRepeatCount = 2), 1),
    )

    @Test fun unlimitedIsBoundedOnlyByTheSimulator() =
        assertEquals(List(50) { 3 }, simulate(RepeatSettings(scope = RepeatScope.VERSE, verseRepeatCount = 0), 3, maxEvents = 50))
}
