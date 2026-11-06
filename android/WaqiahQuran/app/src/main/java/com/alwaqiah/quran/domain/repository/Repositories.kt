package com.alwaqiah.quran.domain.repository

import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.model.Bookmark
import com.alwaqiah.quran.domain.model.ReadingPosition
import com.alwaqiah.quran.domain.model.Reciter
import com.alwaqiah.quran.domain.model.SurahInfo
import com.alwaqiah.quran.domain.model.ThemeMode
import com.alwaqiah.quran.domain.model.TranslationEdition
import com.alwaqiah.quran.domain.model.UserPreferences
import com.alwaqiah.quran.domain.model.Verse
import com.alwaqiah.quran.domain.model.VerseTiming
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun observeSurah(): Flow<SurahInfo?>
    fun observeVerses(): Flow<List<Verse>>
    fun observeTranslationEditions(): Flow<List<TranslationEdition>>
    /** Map of verse number to translated text for the given edition. */
    fun observeTranslation(editionId: String): Flow<Map<Int, String>>
}

interface ReciterRepository {
    fun observeReciters(): Flow<List<Reciter>>
    suspend fun getTimings(reciterId: Int): List<VerseTiming>
}

interface BookmarkRepository {
    fun observeBookmarks(): Flow<List<Bookmark>>
    fun observeBookmarkedVerseNumbers(): Flow<Set<Int>>
    suspend fun toggle(verseNumber: Int)
    suspend fun remove(verseNumber: Int)
}

interface ReadingHistoryRepository {
    fun observeLastRead(): Flow<ReadingPosition?>
    suspend fun record(verseNumber: Int)
}

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setArabicFontSize(sizeSp: Float)
    suspend fun setArabicScript(script: ArabicScript)
    suspend fun setPrimaryTranslation(editionId: String)
    suspend fun setShowEnglishTranslation(show: Boolean)
    suspend fun setReciter(reciterId: Int)
    suspend fun setPlaybackSpeed(speed: Float)
    suspend fun setSpeedStep(step: Float)
    suspend fun setRepeatCount(count: Int)
    suspend fun setPauseBetweenRepeats(ms: Long)
    suspend fun setAutoScroll(enabled: Boolean)
    suspend fun setHapticFeedback(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDownloadOnWifiOnly(enabled: Boolean)
}
