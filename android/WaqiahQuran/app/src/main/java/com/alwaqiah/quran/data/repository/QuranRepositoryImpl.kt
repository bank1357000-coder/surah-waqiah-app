package com.alwaqiah.quran.data.repository

import com.alwaqiah.quran.data.local.dao.QuranDao
import com.alwaqiah.quran.data.mapper.toDomain
import com.alwaqiah.quran.data.seed.ContentSeeder
import com.alwaqiah.quran.domain.model.SurahInfo
import com.alwaqiah.quran.domain.model.TranslationEdition
import com.alwaqiah.quran.domain.model.Verse
import com.alwaqiah.quran.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Runs [block] only after bundled content has been imported. */
internal fun <T> ContentSeeder.seeded(block: () -> Flow<T>): Flow<T> = flow {
    ensureSeeded()
    emitAll(block())
}

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val dao: QuranDao,
    private val seeder: ContentSeeder,
) : QuranRepository {

    override fun observeSurah(): Flow<SurahInfo?> =
        seeder.seeded { dao.observeSurah() }.map { it?.toDomain() }.distinctUntilChanged()

    override fun observeVerses(): Flow<List<Verse>> =
        seeder.seeded { dao.observeVerses() }.map { list -> list.map { it.toDomain() } }.distinctUntilChanged()

    override fun observeTranslationEditions(): Flow<List<TranslationEdition>> =
        seeder.seeded { dao.observeEditions() }.map { list -> list.map { it.toDomain() } }.distinctUntilChanged()

    override fun observeTranslation(editionId: String): Flow<Map<Int, String>> =
        seeder.seeded { dao.observeTranslation(editionId) }
            .map { rows -> rows.associate { it.verseNumber to it.text } }
            .distinctUntilChanged()
}
