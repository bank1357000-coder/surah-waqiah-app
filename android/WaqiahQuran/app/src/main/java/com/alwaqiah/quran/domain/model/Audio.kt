package com.alwaqiah.quran.domain.model

data class Reciter(
    val id: Int,
    val nameFa: String,
    val nameEn: String,
    val style: String,
    val audioUrl: String,
    val timingSource: String,
    val localFilePath: String?,
)

/** Verse-level timestamp inside a reciter's full-surah audio file (milliseconds). */
data class VerseTiming(
    val verse: Int,
    val fromMs: Long,
    val toMs: Long,
)
