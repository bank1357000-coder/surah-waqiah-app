package com.alwaqiah.quran.data.download

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.alwaqiah.quran.data.local.dao.ReciterDao
import com.alwaqiah.quran.data.local.entity.ReciterEntity
import com.alwaqiah.quran.di.IoDispatcher
import com.alwaqiah.quran.domain.model.DownloadStatus
import com.alwaqiah.quran.domain.repository.AudioDownloadRepository
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioDownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reciterDao: ReciterDao,
    private val fileStore: AudioFileStore,
    private val preferencesRepository: UserPreferencesRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) : AudioDownloadRepository {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    override fun observeStatuses(): Flow<Map<Int, DownloadStatus>> =
        combine(reciterDao.observeReciters(), workManager.getWorkInfosByTagFlow(TAG)) { reciters, infos ->
            reciters.associate { reciter ->
                val info = infos.filter { tagFor(reciter.id) in it.tags }
                    .firstOrNull { !it.state.isFinished } ?: infos.lastOrNull { tagFor(reciter.id) in it.tags }
                reciter.id to statusFor(reciter, info)
            }
        }.flowOn(io).distinctUntilChanged()

    override fun observeStorageUsageBytes(): Flow<Long> =
        observeStatuses().map { fileStore.totalBytes() }.flowOn(io).distinctUntilChanged()

    private fun statusFor(reciter: ReciterEntity, info: WorkInfo?): DownloadStatus {
        val local = reciter.localFilePath?.let(::File)
        if (local != null && local.isFile) return DownloadStatus.Downloaded(local.length())
        val part = fileStore.partFile(reciter.id)
        return when (info?.state) {
            WorkInfo.State.RUNNING -> DownloadStatus.Downloading(
                downloadedBytes = info.progress.getLong(AudioDownloadWorker.KEY_BYTES, part.length()),
                totalBytes = info.progress.getLong(AudioDownloadWorker.KEY_TOTAL, -1L),
            )
            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> DownloadStatus.Queued
            WorkInfo.State.FAILED -> DownloadStatus.Failed
            else -> if (part.exists() && part.length() > 0) DownloadStatus.Paused(part.length()) else DownloadStatus.NotDownloaded
        }
    }

    override suspend fun start(reciterId: Int) {
        val reciter = reciterDao.getReciter(reciterId) ?: return
        val wifiOnly = preferencesRepository.preferences.first().downloadOnWifiOnly
        val request = OneTimeWorkRequestBuilder<AudioDownloadWorker>()
            .setInputData(workDataOf(AudioDownloadWorker.KEY_RECITER_ID to reciterId, AudioDownloadWorker.KEY_URL to reciter.audioUrl))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .addTag(TAG)
            .addTag(tagFor(reciterId))
            .build()
        workManager.enqueueUniqueWork(uniqueName(reciterId), ExistingWorkPolicy.KEEP, request)
    }

    override fun pause(reciterId: Int) {
        workManager.cancelUniqueWork(uniqueName(reciterId))
    }

    override suspend fun cancel(reciterId: Int) {
        workManager.cancelUniqueWork(uniqueName(reciterId))
        withContext(io) { fileStore.partFile(reciterId).delete() }
    }

    override suspend fun delete(reciterId: Int) {
        cancel(reciterId)
        withContext(io) { fileStore.audioFile(reciterId).delete() }
        reciterDao.setLocalFile(reciterId, null)
    }

    private companion object {
        const val TAG = "audio-download"
        fun tagFor(id: Int) = "audio-download-$id"
        fun uniqueName(id: Int) = "audio-download-unique-$id"
    }
}
