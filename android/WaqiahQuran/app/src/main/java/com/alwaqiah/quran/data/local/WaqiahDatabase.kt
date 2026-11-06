package com.alwaqiah.quran.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alwaqiah.quran.data.local.dao.BookmarkDao
import com.alwaqiah.quran.data.local.dao.MemorizationDao
import com.alwaqiah.quran.data.local.dao.NoteDao
import com.alwaqiah.quran.data.local.dao.QuranDao
import com.alwaqiah.quran.data.local.dao.ReadingHistoryDao
import com.alwaqiah.quran.data.local.dao.ReciterDao
import com.alwaqiah.quran.data.local.entity.BookmarkEntity
import com.alwaqiah.quran.data.local.entity.MemorizationActivityEntity
import com.alwaqiah.quran.data.local.entity.MemorizationProgressEntity
import com.alwaqiah.quran.data.local.entity.NoteEntity
import com.alwaqiah.quran.data.local.entity.ReadingHistoryEntity
import com.alwaqiah.quran.data.local.entity.ReciterEntity
import com.alwaqiah.quran.data.local.entity.SurahEntity
import com.alwaqiah.quran.data.local.entity.TafsirSourceEntity
import com.alwaqiah.quran.data.local.entity.TranslationEditionEntity
import com.alwaqiah.quran.data.local.entity.VerseEntity
import com.alwaqiah.quran.data.local.entity.VerseTimingEntity
import com.alwaqiah.quran.data.local.entity.VerseTranslationEntity

@Database(
    entities = [
        SurahEntity::class,
        VerseEntity::class,
        TranslationEditionEntity::class,
        VerseTranslationEntity::class,
        TafsirSourceEntity::class,
        ReciterEntity::class,
        VerseTimingEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        MemorizationProgressEntity::class,
        ReadingHistoryEntity::class,
        MemorizationActivityEntity::class,
    ],
    version = WaqiahDatabase.VERSION,
    exportSchema = true,
)
abstract class WaqiahDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun reciterDao(): ReciterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun memorizationDao(): MemorizationDao
    abstract fun readingHistoryDao(): ReadingHistoryDao

    companion object {
        const val VERSION = 2
        const val NAME = "waqiah.db"
    }
}
