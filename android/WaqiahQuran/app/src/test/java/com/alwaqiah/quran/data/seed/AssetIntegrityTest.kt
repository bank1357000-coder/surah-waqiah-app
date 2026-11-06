package com.alwaqiah.quran.data.seed

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the integrity of the bundled, verified content.
 * Reads the JSON straight from src/main/assets (unit-test working dir = app module).
 */
class AssetIntegrityTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun asset(name: String) = File("src/main/assets/quran/$name").readText(Charsets.UTF_8)

    @Test
    fun surahHasAllVersesAndTranslations() {
        val surah = json.decodeFromString<SurahAsset>(asset("surah_56.json"))
        assertEquals(56, surah.surah.number)
        assertEquals(96, surah.verses.size)
        assertEquals((1..96).toList(), surah.verses.map { it.number })
        assertTrue(surah.verses.all { it.textHafs.isNotBlank() && it.textTanzil.isNotBlank() })
        // Verse numbers are rendered separately, so the Hafs text must not embed them.
        assertTrue(surah.verses.none { v -> v.textHafs.any { it in '\u0660'..'\u0669' } })
        // Bismillah is stored separately, not inside verse 1.
        assertTrue(!surah.verses.first().textTanzil.startsWith("بِسْمِ"))
        assertEquals(setOf("fa.makarem", "fa.fooladvand", "fa.ansarian", "en.sahih"), surah.translations.map { it.id }.toSet())
        surah.translations.forEach { assertEquals(it.id, 96, it.texts.size) }
    }

    @Test
    fun everyReciterHasMonotonicTimingsForAllVerses() {
        val reciters = json.decodeFromString<RecitersAsset>(asset("reciters.json")).reciters
        assertTrue(reciters.isNotEmpty())
        reciters.forEach { r ->
            assertEquals(96, r.timings.size)
            assertEquals((1..96).toList(), r.timings.map { it.verse })
            r.timings.zipWithNext().forEach { (a, b) ->
                assertTrue("reciter ${r.id} verse ${b.verse}", b.fromMs >= a.fromMs)
            }
            assertTrue(r.audioUrl.startsWith("https://"))
        }
    }
}
