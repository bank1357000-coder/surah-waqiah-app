package com.alwaqiah.quran.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "surah")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val nameFa: String,
    val nameEn: String,
    val revelation: String,
    val verseCount: Int,
    val bismillahHafs: String,
    val bismillahTanzil: String,
    /** Version of the bundled content that produced this row (see ContentSeeder). */
    val contentVersion: Int,
)

@Entity(tableName = "verses")
data class VerseEntity(
    @PrimaryKey val number: Int,
    val textHafs: String,
    val textTanzil: String,
    val juz: Int,
    val page: Int,
)

@Entity(tableName = "translation_editions")
data class TranslationEditionEntity(
    @PrimaryKey val id: String,
    val language: String,
    val name: String,
    val translator: String,
    val source: String,
)

@Entity(
    tableName = "verse_translations",
    primaryKeys = ["editionId", "verseNumber"],
    foreignKeys = [
        ForeignKey(
            entity = TranslationEditionEntity::class,
            parentColumns = ["id"],
            childColumns = ["editionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("verseNumber")],
)
data class VerseTranslationEntity(
    val editionId: String,
    val verseNumber: Int,
    val text: String,
)

/** Tafsir metadata only. Content is added in Phase 4 together with verified sources. */
@Entity(tableName = "tafsir_sources")
data class TafsirSourceEntity(
    @PrimaryKey val id: String,
    val language: String,
    val title: String,
    val author: String,
    val source: String,
    val license: String,
    val hasAudio: Boolean,
    val audioUrl: String?,
)
