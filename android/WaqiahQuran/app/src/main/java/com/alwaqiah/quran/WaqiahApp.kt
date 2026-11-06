package com.alwaqiah.quran

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.alwaqiah.quran.data.seed.ContentSeeder
import com.alwaqiah.quran.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WaqiahApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var contentSeeder: ContentSeeder
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // Warm the offline database off the main thread so the reader opens instantly.
        // Failures are not fatal here: screens call ensureSeeded() again and show a retryable error state.
        appScope.launch {
            runCatching { contentSeeder.ensureSeeded() }
                .onFailure { Log.e("WaqiahApp", "Content import failed", it) }
        }
    }
}
