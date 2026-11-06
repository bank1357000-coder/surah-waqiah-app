package com.alwaqiah.quran.domain.model

enum class Revelation { MECCAN, MEDINAN }

data class SurahInfo(
    val number: Int,
    val nameArabic: String,
    val nameFa: String,
    val nameEn: String,
    val revelation: Revelation,
    val verseCount: Int,
    val bismillahHafs: String,
    val bismillahTanzil: String,
)

data class Verse(
    val number: Int,
    /** KFGQPC Hafs text (Uthman Taha script), rendered with the KFGQPC Uthmanic Hafs font. */
    val textHafs: String,
    /** Tanzil Uthmani text, rendered with the Amiri Quran font. */
    val textTanzil: String,
    val juz: Int,
    val page: Int,
)

data class TranslationEdition(
    val id: String,
    val language: String,
    val name: String,
    val translator: String,
    val source: String,
)

/** A verse prepared for display in the reader. */
data class ReaderVerse(
    val verse: Verse,
    val primaryTranslation: String?,
    val englishTranslation: String?,
    val isBookmarked: Boolean,
)
