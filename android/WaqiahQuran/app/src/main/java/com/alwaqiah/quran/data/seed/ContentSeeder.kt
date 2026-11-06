package com.alwaqiah.quran.data.seed

import android.content.Context
import androidx.room.withTransaction
import com.alwaqiah.quran.data.local.WaqiahDatabase
import com.alwaqiah.quran.data.local.entity.ReciterEntity
import com.alwaqiah.quran.data.local.entity.SurahEntity
import com.alwaqiah.quran.data.local.entity.TranslationEditionEntity
import com.alwaqiah.quran.data.local.entity.VerseEntity
import com.alwaqiah.quran.data.local.entity.VerseTimingEntity
import com.alwaqiah.quran.data.local.entity.VerseTranslationEntity
import com.alwaqiah.quran.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Imports the bundled, verified Quran content into Room on first launch (offline-first).
 * Idempotent: safe to call from any repository before reading.
 */
@Singleton
class ContentSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WaqiahDatabase,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()

    @Volatile
    private var seeded = false

    suspend fun ensureSeeded() {
        if (seeded) return
        mutex.withLock {
            if (seeded) return
            withContext(ioDispatcher) {
                val surah: SurahAsset = readAsset(SURAH_ASSET)
                if (needsSeeding(surah.contentVersion)) seed(surah)
            }
            seeded = true
        }
    }

    private suspend fun needsSeeding(assetVersion: Int): Boolean {
        val quranDao = database.quranDao()
        return (quranDao.contentVersion() ?: 0) < assetVersion ||
            quranDao.verseCount() != EXPECTED_VERSES ||
            quranDao.translationRowCount() == 0 ||
            database.reciterDao().count() == 0
    }

    private suspend fun seed(surah: SurahAsset) {
        val reciters: RecitersAsset = readAsset(RECITERS_ASSET)
        validate(surah, reciters)

        database.withTransaction {
            val quranDao = database.quranDao()
            quranDao.upsertSurah(
                SurahEntity(
                    number = surah.surah.number,
                    nameArabic = surah.surah.nameArabic,
                    nameFa = surah.surah.nameFa,
                    nameEn = surah.surah.nameEn,
                    revelation = surah.surah.revelation,
                    verseCount = surah.surah.verseCount,
                    bismillahHafs = surah.bismillahHafs,
                    bismillahTanzil = surah.bismillahTanzil,
                    contentVersion = surah.contentVersion,
                )
            )
            quranDao.upsertVerses(
                surah.verses.map { VerseEntity(it.number, it.textHafs, it.textTanzil, it.juz, it.page) }
            )
            quranDao.upsertEditions(
                surah.translations.map {
                    TranslationEditionEntity(it.id, it.language, it.nameFa, it.translator, it.source)
                }
            )
            quranDao.upsertTranslations(
                surah.translations.flatMap { edition ->
                    edition.texts.mapIndexed { index, text ->
                        VerseTranslationEntity(edition.id, index + 1, text)
                    }
                }
            )

            val reciterDao = database.reciterDao()
            reciterDao.upsertReciters(
                reciters.reciters.map {
                    ReciterEntity(it.id, it.nameFa, it.nameEn, it.style, it.audioUrl, it.timingSource, null)
                }
            )
            reciterDao.upsertTimings(
                reciters.reciters.flatMap { reciter ->
                    reciter.timings.map { VerseTimingEntity(reciter.id, it.verse, it.fromMs, it.toMs) }
                }
            )
        }
    }

    private fun validate(surah: SurahAsset, reciters: RecitersAsset) {
        check(surah.verses.size == EXPECTED_VERSES) { "Expected $EXPECTED_VERSES verses, found ${surah.verses.size}" }
        surah.translations.forEach {
            check(it.texts.size == EXPECTED_VERSES) { "Translation ${it.id} is incomplete" }
        }
        reciters.reciters.forEach {
            check(it.timings.size == EXPECTED_VERSES) { "Timings for reciter ${it.id} are incomplete" }
        }
    }

    private inline fun <reified T> readAsset(path: String): T =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { json.decodeFromString<T>(it.readText()) }

    companion object {
        const val EXPECTED_VERSES = 96
        const val SURAH_ASSET = "quran/surah_56.json"
        const val RECITERS_ASSET = "quran/reciters.json"
    }
}
