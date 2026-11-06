package com.alwaqiah.quran.data.seed

import kotlinx.serialization.Serializable

/** Shape of assets/quran/surah_56.json (generated from verified sources, see docs/CONTENT_SOURCES.md). */
@Serializable
data class SurahAsset(
    /** Bump when bundled content changes so existing installs re-import it. */
    val contentVersion: Int = 1,
    val surah: SurahMetaAsset,
    val bismillahHafs: String,
    val bismillahTanzil: String,
    val provenance: Map<String, String> = emptyMap(),
    val verses: List<VerseAsset>,
    val translations: List<TranslationAsset>,
)

@Serializable
data class SurahMetaAsset(
    val number: Int,
    val nameArabic: String,
    val nameFa: String,
    val nameEn: String,
    val revelation: String,
    val verseCount: Int,
)

@Serializable
data class VerseAsset(
    val number: Int,
    val textHafs: String,
    val textTanzil: String,
    val juz: Int,
    val page: Int,
)

@Serializable
data class TranslationAsset(
    val id: String,
    val language: String,
    val nameFa: String,
    val translator: String,
    val source: String,
    val texts: List<String>,
)

/** Shape of assets/quran/reciters.json. */
@Serializable
data class RecitersAsset(val reciters: List<ReciterAsset>)

@Serializable
data class ReciterAsset(
    val id: Int,
    val nameFa: String,
    val nameEn: String,
    val style: String,
    val audioUrl: String,
    val timingSource: String,
    val timings: List<TimingAsset>,
)

@Serializable
data class TimingAsset(val verse: Int, val fromMs: Long, val toMs: Long)
