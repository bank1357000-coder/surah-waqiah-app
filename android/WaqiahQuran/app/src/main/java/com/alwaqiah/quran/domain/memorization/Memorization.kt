package com.alwaqiah.quran.domain.memorization

import kotlinx.coroutines.flow.Flow

enum class MemorizationStatus { NEW, LEARNING, MEMORIZED, NEEDS_REVISION }

data class MemorizationProgress(
    val verseNumber: Int,
    val repetitions: Int,
    val status: MemorizationStatus,
    val lastPracticedAt: Long,
)

data class DailyActivity(
    val date: String,
    val versesPracticed: Int = 0,
    val repetitions: Int = 0,
    val newlyMemorized: Int = 0,
)

/** Persisted so an interrupted session can be resumed exactly where it stopped. */
data class MemorizationSession(
    val rangeStart: Int = 1,
    val rangeEnd: Int = 5,
    val verseRepeatCount: Int = 5,
    val pauseBetweenRepeatsMs: Long = 1_500L,
    val hideArabic: Boolean = false,
    val hideTranslation: Boolean = false,
    val lastVerse: Int = 1,
    val dailyGoalVerses: Int = 3,
)

data class MemorizationSummary(
    val memorized: Int,
    val learning: Int,
    val needsRevision: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total == 0) 0f else memorized.toFloat() / total
}

object MemorizationStats {
    fun summarize(progress: Map<Int, MemorizationProgress>, total: Int = 96) = MemorizationSummary(
        memorized = progress.values.count { it.status == MemorizationStatus.MEMORIZED },
        learning = progress.values.count { it.status == MemorizationStatus.LEARNING },
        needsRevision = progress.values.count { it.status == MemorizationStatus.NEEDS_REVISION },
        total = total,
    )

    fun goalFraction(today: DailyActivity?, goalVerses: Int): Float =
        if (goalVerses <= 0) 1f else ((today?.versesPracticed ?: 0).toFloat() / goalVerses).coerceIn(0f, 1f)

    /** Status after one more completed repetition. Explicit user marks are never downgraded. */
    fun statusAfterRepetition(current: MemorizationStatus?): MemorizationStatus = when (current) {
        null, MemorizationStatus.NEW -> MemorizationStatus.LEARNING
        else -> current
    }
}

interface MemorizationRepository {
    fun observeProgress(): Flow<Map<Int, MemorizationProgress>>
    fun observeToday(): Flow<DailyActivity>
    fun observeRecentActivity(days: Int = 7): Flow<List<DailyActivity>>
    val session: Flow<MemorizationSession>
    suspend fun saveSession(session: MemorizationSession)
    suspend fun recordRepetition(verseNumber: Int)
    suspend fun setStatus(verseNumber: Int, status: MemorizationStatus)
}
