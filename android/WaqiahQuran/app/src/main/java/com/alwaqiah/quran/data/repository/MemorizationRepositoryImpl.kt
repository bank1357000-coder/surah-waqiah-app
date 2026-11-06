package com.alwaqiah.quran.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.room.withTransaction
import com.alwaqiah.quran.data.local.WaqiahDatabase
import com.alwaqiah.quran.data.local.entity.MemorizationActivityEntity
import com.alwaqiah.quran.data.local.entity.MemorizationProgressEntity
import com.alwaqiah.quran.domain.memorization.DailyActivity
import com.alwaqiah.quran.domain.memorization.MemorizationProgress
import com.alwaqiah.quran.domain.memorization.MemorizationRepository
import com.alwaqiah.quran.domain.memorization.MemorizationSession
import com.alwaqiah.quran.domain.memorization.MemorizationStats
import com.alwaqiah.quran.domain.memorization.MemorizationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemorizationRepositoryImpl @Inject constructor(
    private val database: WaqiahDatabase,
    private val dataStore: DataStore<Preferences>,
) : MemorizationRepository {

    private val dao get() = database.memorizationDao()

    private object Keys {
        val START = intPreferencesKey("memo_range_start")
        val END = intPreferencesKey("memo_range_end")
        val REPEAT = intPreferencesKey("memo_verse_repeat")
        val PAUSE = longPreferencesKey("memo_pause_ms")
        val HIDE_AR = booleanPreferencesKey("memo_hide_arabic")
        val HIDE_TR = booleanPreferencesKey("memo_hide_translation")
        val LAST = intPreferencesKey("memo_last_verse")
        val GOAL = intPreferencesKey("memo_daily_goal")
    }

    private fun today(): String = LocalDate.now().toString()
    private fun startOfToday(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    override fun observeProgress(): Flow<Map<Int, MemorizationProgress>> = dao.observeAll().map { rows ->
        rows.associate { e ->
            e.verseNumber to MemorizationProgress(
                e.verseNumber,
                e.repetitions,
                runCatching { MemorizationStatus.valueOf(e.status) }.getOrDefault(MemorizationStatus.NEW),
                e.lastPracticedAt,
            )
        }
    }.distinctUntilChanged()

    // Note: the date key is computed when collection starts; screens re-collect on resume.
    override fun observeToday(): Flow<DailyActivity> = dao.observeActivity(today()).map { it.toDomain(today()) }

    override fun observeRecentActivity(days: Int): Flow<List<DailyActivity>> =
        dao.observeRecentActivity(days).map { list -> list.map { it.toDomain(it.date) } }

    override val session: Flow<MemorizationSession> = dataStore.data.map { p ->
        val d = MemorizationSession()
        MemorizationSession(
            rangeStart = p[Keys.START] ?: d.rangeStart,
            rangeEnd = p[Keys.END] ?: d.rangeEnd,
            verseRepeatCount = p[Keys.REPEAT] ?: d.verseRepeatCount,
            pauseBetweenRepeatsMs = p[Keys.PAUSE] ?: d.pauseBetweenRepeatsMs,
            hideArabic = p[Keys.HIDE_AR] ?: d.hideArabic,
            hideTranslation = p[Keys.HIDE_TR] ?: d.hideTranslation,
            lastVerse = p[Keys.LAST] ?: d.lastVerse,
            dailyGoalVerses = p[Keys.GOAL] ?: d.dailyGoalVerses,
        )
    }.distinctUntilChanged()

    override suspend fun saveSession(session: MemorizationSession) {
        dataStore.edit {
            it[Keys.START] = minOf(session.rangeStart, session.rangeEnd).coerceIn(1, 96)
            it[Keys.END] = maxOf(session.rangeStart, session.rangeEnd).coerceIn(1, 96)
            it[Keys.REPEAT] = session.verseRepeatCount.coerceAtLeast(0)
            it[Keys.PAUSE] = session.pauseBetweenRepeatsMs.coerceIn(0L, 10_000L)
            it[Keys.HIDE_AR] = session.hideArabic
            it[Keys.HIDE_TR] = session.hideTranslation
            it[Keys.LAST] = session.lastVerse.coerceIn(1, 96)
            it[Keys.GOAL] = session.dailyGoalVerses.coerceIn(1, 96)
        }
    }

    override suspend fun recordRepetition(verseNumber: Int) = database.withTransaction {
        val now = System.currentTimeMillis()
        val existing = dao.get(verseNumber)
        val firstTimeToday = existing == null || existing.lastPracticedAt < startOfToday()
        val current = existing?.let { runCatching { MemorizationStatus.valueOf(it.status) }.getOrNull() }
        dao.upsert(
            MemorizationProgressEntity(
                verseNumber = verseNumber,
                repetitions = (existing?.repetitions ?: 0) + 1,
                status = MemorizationStats.statusAfterRepetition(current).name,
                lastPracticedAt = now,
            )
        )
        val day = dao.activity(today()) ?: MemorizationActivityEntity(today(), 0, 0, 0)
        dao.upsertActivity(
            day.copy(
                repetitions = day.repetitions + 1,
                versesPracticed = day.versesPracticed + if (firstTimeToday) 1 else 0,
            )
        )
    }

    override suspend fun setStatus(verseNumber: Int, status: MemorizationStatus) = database.withTransaction {
        val existing = dao.get(verseNumber)
        dao.upsert(
            MemorizationProgressEntity(
                verseNumber = verseNumber,
                repetitions = existing?.repetitions ?: 0,
                status = status.name,
                lastPracticedAt = System.currentTimeMillis(),
            )
        )
        if (status == MemorizationStatus.MEMORIZED && existing?.status != MemorizationStatus.MEMORIZED.name) {
            val day = dao.activity(today()) ?: MemorizationActivityEntity(today(), 0, 0, 0)
            dao.upsertActivity(day.copy(newlyMemorized = day.newlyMemorized + 1))
        }
    }

    private fun MemorizationActivityEntity?.toDomain(date: String) =
        this?.let { DailyActivity(it.date, it.versesPracticed, it.repetitions, it.newlyMemorized) } ?: DailyActivity(date)
}
