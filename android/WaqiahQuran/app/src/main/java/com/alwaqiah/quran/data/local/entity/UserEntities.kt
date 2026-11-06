package com.alwaqiah.quran.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks", indices = [Index(value = ["verseNumber"], unique = true)])
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val verseNumber: Int,
    val createdAt: Long,
    val note: String? = null,
)

@Entity(tableName = "notes", indices = [Index("verseNumber")])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val verseNumber: Int,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "memorization_progress")
data class MemorizationProgressEntity(
    @PrimaryKey val verseNumber: Int,
    val repetitions: Int,
    /** NEW, LEARNING or MEMORIZED */
    val status: String,
    val lastPracticedAt: Long,
)

@Entity(tableName = "reading_history", indices = [Index("visitedAt")])
data class ReadingHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val verseNumber: Int,
    val visitedAt: Long,
)
