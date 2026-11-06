package com.alwaqiah.quran.domain.usecase

import com.alwaqiah.quran.domain.model.VerseTiming
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindVerseAtPositionUseCaseTest {
    private val find = FindVerseAtPositionUseCase()
    private val timings = listOf(
        VerseTiming(1, 0, 3_550),
        VerseTiming(2, 3_550, 7_000),
        VerseTiming(3, 7_000, 11_200),
    )

    @Test fun emptyReturnsNull() = assertNull(find(emptyList(), 1_000))
    @Test fun startOfFile() = assertEquals(1, find(timings, 0))
    @Test fun insideVerse() = assertEquals(2, find(timings, 5_000))
    @Test fun exactBoundaryBelongsToNextVerse() = assertEquals(2, find(timings, 3_550))
    @Test fun pastEndReturnsLastVerse() = assertEquals(3, find(timings, 99_999))
    @Test fun negativePositionReturnsFirstVerse() = assertEquals(1, find(timings, -10))
}
