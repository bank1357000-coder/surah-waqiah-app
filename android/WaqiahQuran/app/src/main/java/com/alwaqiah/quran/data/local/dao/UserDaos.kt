package com.alwaqiah.quran.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.alwaqiah.quran.data.local.entity.BookmarkEntity
import com.alwaqiah.quran.data.local.entity.MemorizationProgressEntity
import com.alwaqiah.quran.data.local.entity.NoteEntity
import com.alwaqiah.quran.data.local.entity.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT verseNumber FROM bookmarks")
    fun observeVerseNumbers(): Flow<List<Int>>

    @Query("SELECT * FROM bookmarks WHERE verseNumber = :verseNumber LIMIT 1")
    suspend fun find(verseNumber: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE verseNumber = :verseNumber")
    suspend fun delete(verseNumber: Int)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE verseNumber = :verseNumber ORDER BY updatedAt DESC")
    fun observeForVerse(verseNumber: Int): Flow<List<NoteEntity>>

    @Upsert suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface MemorizationDao {
    @Query("SELECT * FROM memorization_progress ORDER BY verseNumber")
    fun observeAll(): Flow<List<MemorizationProgressEntity>>

    @Upsert suspend fun upsert(progress: MemorizationProgressEntity)
}

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY visitedAt DESC LIMIT 1")
    fun observeLast(): Flow<ReadingHistoryEntity?>

    @Insert
    suspend fun insert(entry: ReadingHistoryEntity)

    @Query(
        "DELETE FROM reading_history WHERE id NOT IN " +
            "(SELECT id FROM reading_history ORDER BY visitedAt DESC LIMIT :keep)"
    )
    suspend fun prune(keep: Int)
}
