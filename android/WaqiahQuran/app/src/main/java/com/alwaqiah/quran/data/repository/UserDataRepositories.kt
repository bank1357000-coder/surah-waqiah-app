package com.alwaqiah.quran.data.repository

import com.alwaqiah.quran.data.local.dao.BookmarkDao
import com.alwaqiah.quran.data.local.dao.ReadingHistoryDao
import com.alwaqiah.quran.data.local.dao.ReciterDao
import com.alwaqiah.quran.data.local.entity.BookmarkEntity
import com.alwaqiah.quran.data.local.entity.ReadingHistoryEntity
import com.alwaqiah.quran.data.mapper.toDomain
import com.alwaqiah.quran.data.seed.ContentSeeder
import com.alwaqiah.quran.domain.model.Bookmark
import com.alwaqiah.quran.domain.model.ReadingPosition
import com.alwaqiah.quran.domain.model.Reciter
import com.alwaqiah.quran.domain.model.VerseTiming
import com.alwaqiah.quran.domain.repository.BookmarkRepository
import com.alwaqiah.quran.domain.repository.ReadingHistoryRepository
import com.alwaqiah.quran.domain.repository.ReciterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReciterRepositoryImpl @Inject constructor(
    private val dao: ReciterDao,
    private val seeder: ContentSeeder,
) : ReciterRepository {
    override fun observeReciters(): Flow<List<Reciter>> =
        seeder.seeded { dao.observeReciters() }.map { list -> list.map { it.toDomain() } }.distinctUntilChanged()

    override suspend fun getTimings(reciterId: Int): List<VerseTiming> {
        seeder.ensureSeeded()
        return dao.getTimings(reciterId).map { it.toDomain() }
    }
}

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val dao: BookmarkDao,
) : BookmarkRepository {
    override fun observeBookmarks(): Flow<List<Bookmark>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeBookmarkedVerseNumbers(): Flow<Set<Int>> =
        dao.observeVerseNumbers().map { it.toSet() }.distinctUntilChanged()

    override suspend fun toggle(verseNumber: Int) {
        if (dao.find(verseNumber) != null) {
            dao.delete(verseNumber)
        } else {
            dao.insert(BookmarkEntity(verseNumber = verseNumber, createdAt = System.currentTimeMillis()))
        }
    }

    override suspend fun remove(verseNumber: Int) = dao.delete(verseNumber)
}

@Singleton
class ReadingHistoryRepositoryImpl @Inject constructor(
    private val dao: ReadingHistoryDao,
) : ReadingHistoryRepository {
    override fun observeLastRead(): Flow<ReadingPosition?> = dao.observeLast().map { it?.toDomain() }

    override suspend fun record(verseNumber: Int) {
        dao.insert(ReadingHistoryEntity(verseNumber = verseNumber, visitedAt = System.currentTimeMillis()))
        dao.prune(keep = HISTORY_LIMIT)
    }

    private companion object {
        const val HISTORY_LIMIT = 200
    }
}
