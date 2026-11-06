package com.alwaqiah.quran.di

import com.alwaqiah.quran.data.preferences.UserPreferencesRepositoryImpl
import com.alwaqiah.quran.data.repository.BookmarkRepositoryImpl
import com.alwaqiah.quran.data.repository.QuranRepositoryImpl
import com.alwaqiah.quran.data.repository.ReadingHistoryRepositoryImpl
import com.alwaqiah.quran.data.repository.ReciterRepositoryImpl
import com.alwaqiah.quran.domain.repository.BookmarkRepository
import com.alwaqiah.quran.domain.repository.QuranRepository
import com.alwaqiah.quran.domain.repository.ReadingHistoryRepository
import com.alwaqiah.quran.domain.repository.ReciterRepository
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindQuranRepository(impl: QuranRepositoryImpl): QuranRepository
    @Binds abstract fun bindReciterRepository(impl: ReciterRepositoryImpl): ReciterRepository
    @Binds abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository
    @Binds abstract fun bindHistoryRepository(impl: ReadingHistoryRepositoryImpl): ReadingHistoryRepository
    @Binds abstract fun bindPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
