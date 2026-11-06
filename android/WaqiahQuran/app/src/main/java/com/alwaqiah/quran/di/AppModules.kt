package com.alwaqiah.quran.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.alwaqiah.quran.data.local.Migrations
import com.alwaqiah.quran.data.local.WaqiahDatabase
import com.alwaqiah.quran.data.local.dao.BookmarkDao
import com.alwaqiah.quran.data.local.dao.MemorizationDao
import com.alwaqiah.quran.data.local.dao.NoteDao
import com.alwaqiah.quran.data.local.dao.QuranDao
import com.alwaqiah.quran.data.local.dao.ReadingHistoryDao
import com.alwaqiah.quran.data.local.dao.ReciterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("user_preferences") })
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WaqiahDatabase =
        Room.databaseBuilder(context, WaqiahDatabase::class.java, WaqiahDatabase.NAME)
            .addMigrations(*Migrations.ALL)
            .build()

    @Provides fun provideQuranDao(db: WaqiahDatabase): QuranDao = db.quranDao()
    @Provides fun provideReciterDao(db: WaqiahDatabase): ReciterDao = db.reciterDao()
    @Provides fun provideBookmarkDao(db: WaqiahDatabase): BookmarkDao = db.bookmarkDao()
    @Provides fun provideNoteDao(db: WaqiahDatabase): NoteDao = db.noteDao()
    @Provides fun provideMemorizationDao(db: WaqiahDatabase): MemorizationDao = db.memorizationDao()
    @Provides fun provideReadingHistoryDao(db: WaqiahDatabase): ReadingHistoryDao = db.readingHistoryDao()
}
