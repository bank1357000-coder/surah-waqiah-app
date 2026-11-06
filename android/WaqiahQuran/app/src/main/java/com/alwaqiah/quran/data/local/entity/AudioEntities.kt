package com.alwaqiah.quran.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Audio lives on a CDN; only URL / optional local download path is stored. */
@Entity(tableName = "reciters")
data class ReciterEntity(
    @PrimaryKey val id: Int,
    val nameFa: String,
    val nameEn: String,
    val style: String,
    val audioUrl: String,
    val timingSource: String,
    val localFilePath: String?,
)

@Entity(
    tableName = "verse_timings",
    primaryKeys = ["reciterId", "verseNumber"],
    foreignKeys = [
        ForeignKey(
            entity = ReciterEntity::class,
            parentColumns = ["id"],
            childColumns = ["reciterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class VerseTimingEntity(
    val reciterId: Int,
    val verseNumber: Int,
    val fromMs: Long,
    val toMs: Long,
)
