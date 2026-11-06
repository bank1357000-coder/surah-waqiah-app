package com.alwaqiah.quran.domain.usecase

import com.alwaqiah.quran.domain.model.VerseTiming
import javax.inject.Inject

/**
 * Resolves which verse is being recited at [positionMs] using verified verse timings.
 * Timings must be sorted by verse / start time (as stored). Binary search, O(log n).
 */
class FindVerseAtPositionUseCase @Inject constructor() {
    operator fun invoke(timings: List<VerseTiming>, positionMs: Long): Int? {
        if (timings.isEmpty()) return null
        var low = 0
        var high = timings.lastIndex
        var match = 0
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (timings[mid].fromMs <= positionMs) {
                match = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return timings[match].verse
    }
}
