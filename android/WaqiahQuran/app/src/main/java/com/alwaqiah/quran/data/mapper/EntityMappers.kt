package com.alwaqiah.quran.data.mapper

import com.alwaqiah.quran.data.local.entity.BookmarkEntity
import com.alwaqiah.quran.data.local.entity.ReadingHistoryEntity
import com.alwaqiah.quran.data.local.entity.ReciterEntity
import com.alwaqiah.quran.data.local.entity.SurahEntity
import com.alwaqiah.quran.data.local.entity.TranslationEditionEntity
import com.alwaqiah.quran.data.local.entity.VerseEntity
import com.alwaqiah.quran.data.local.entity.VerseTimingEntity
import com.alwaqiah.quran.domain.model.Bookmark
import com.alwaqiah.quran.domain.model.ReadingPosition
import com.alwaqiah.quran.domain.model.Reciter
import com.alwaqiah.quran.domain.model.Revelation
import com.alwaqiah.quran.domain.model.SurahInfo
import com.alwaqiah.quran.domain.model.TranslationEdition
import com.alwaqiah.quran.domain.model.Verse
import com.alwaqiah.quran.domain.model.VerseTiming

fun SurahEntity.toDomain() = SurahInfo(
    number = number,
    nameArabic = nameArabic,
    nameFa = nameFa,
    nameEn = nameEn,
    revelation = if (revelation.equals("medinan", ignoreCase = true)) Revelation.MEDINAN else Revelation.MECCAN,
    verseCount = verseCount,
    bismillahHafs = bismillahHafs,
    bismillahTanzil = bismillahTanzil,
)

fun VerseEntity.toDomain() = Verse(number, textHafs, textTanzil, juz, page)

fun TranslationEditionEntity.toDomain() = TranslationEdition(id, language, name, translator, source)

fun ReciterEntity.toDomain() = Reciter(id, nameFa, nameEn, style, audioUrl, timingSource, localFilePath)

fun VerseTimingEntity.toDomain() = VerseTiming(verseNumber, fromMs, toMs)

fun BookmarkEntity.toDomain() = Bookmark(id, verseNumber, createdAt, note)

fun ReadingHistoryEntity.toDomain() = ReadingPosition(verseNumber, visitedAt)
