package com.alwaqiah.quran.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.alwaqiah.quran.data.local.entity.SurahEntity
import com.alwaqiah.quran.data.local.entity.TranslationEditionEntity
import com.alwaqiah.quran.data.local.entity.VerseEntity
import com.alwaqiah.quran.data.local.entity.VerseTranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT COUNT(*) FROM verses")
    suspend fun verseCount(): Int

    @Query("SELECT COUNT(*) FROM verse_translations")
    suspend fun translationRowCount(): Int

    @Query("SELECT contentVersion FROM surah LIMIT 1")
    suspend fun contentVersion(): Int?

    @Query("SELECT * FROM surah LIMIT 1")
    fun observeSurah(): Flow<SurahEntity?>

    @Query("SELECT * FROM verses ORDER BY number")
    fun observeVerses(): Flow<List<VerseEntity>>

    @Query("SELECT * FROM translation_editions ORDER BY language DESC, id")
    fun observeEditions(): Flow<List<TranslationEditionEntity>>

    @Query("SELECT * FROM verse_translations WHERE editionId = :editionId ORDER BY verseNumber")
    fun observeTranslation(editionId: String): Flow<List<VerseTranslationEntity>>

    @Upsert suspend fun upsertSurah(surah: SurahEntity)
    @Upsert suspend fun upsertVerses(verses: List<VerseEntity>)
    @Upsert suspend fun upsertEditions(editions: List<TranslationEditionEntity>)
    @Upsert suspend fun upsertTranslations(rows: List<VerseTranslationEntity>)
}
