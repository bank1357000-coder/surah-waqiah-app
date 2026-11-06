package com.alwaqiah.quran.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alwaqiah.quran.data.local.entity.BookmarkEntity
import com.alwaqiah.quran.data.local.entity.TranslationEditionEntity
import com.alwaqiah.quran.data.local.entity.VerseEntity
import com.alwaqiah.quran.data.local.entity.VerseTranslationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaqiahDatabaseTest {
    private lateinit var db: WaqiahDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WaqiahDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun versesAndTranslationsRoundTrip() = runTest {
        val dao = db.quranDao()
        dao.upsertVerses(listOf(VerseEntity(2, "b", "b", 27, 534), VerseEntity(1, "a", "a", 27, 534)))
        dao.upsertEditions(listOf(TranslationEditionEntity("fa.test", "fa", "t", "t", "s")))
        dao.upsertTranslations(listOf(VerseTranslationEntity("fa.test", 1, "x")))

        assertEquals(listOf(1, 2), dao.observeVerses().first().map { it.number })
        assertEquals("x", dao.observeTranslation("fa.test").first().single().text)
    }

    @Test
    fun bookmarkIsUniquePerVerse() = runTest {
        val dao = db.bookmarkDao()
        dao.insert(BookmarkEntity(verseNumber = 5, createdAt = 1))
        dao.insert(BookmarkEntity(verseNumber = 5, createdAt = 2))
        assertEquals(1, dao.observeAll().first().size)
        dao.delete(5)
        assertEquals(0, dao.observeAll().first().size)
    }
}
