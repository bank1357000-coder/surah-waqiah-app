package com.alwaqiah.quran.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.alwaqiah.quran.data.local.entity.ReciterEntity
import com.alwaqiah.quran.data.local.entity.VerseTimingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReciterDao {
    @Query("SELECT COUNT(*) FROM reciters")
    suspend fun count(): Int

    @Query("SELECT * FROM reciters ORDER BY nameFa")
    fun observeReciters(): Flow<List<ReciterEntity>>

    @Query("SELECT * FROM verse_timings WHERE reciterId = :reciterId ORDER BY verseNumber")
    suspend fun getTimings(reciterId: Int): List<VerseTimingEntity>

    @Query("UPDATE reciters SET localFilePath = :path WHERE id = :reciterId")
    suspend fun setLocalFile(reciterId: Int, path: String?)

    @Upsert suspend fun upsertReciters(reciters: List<ReciterEntity>)
    @Upsert suspend fun upsertTimings(timings: List<VerseTimingEntity>)
}
