package com.alwaqiah.quran.data.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.alwaqiah.quran.data.local.dao.ReciterDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/**
 * Downloads one reciter's full-surah MP3 with HTTP Range resume.
 * Pausing = cancelling the work; the `.part` file is kept and the next run resumes from it.
 * Transient network errors return [Result.retry] (WorkManager backoff = automatic recovery).
 */
@HiltWorker
class AudioDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val fileStore: AudioFileStore,
    private val reciterDao: ReciterDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val reciterId = inputData.getInt(KEY_RECITER_ID, -1)
        val url = inputData.getString(KEY_URL)
        if (reciterId < 0 || url.isNullOrBlank()) return@withContext Result.failure()

        val part = fileStore.partFile(reciterId)
        val target = fileStore.audioFile(reciterId)
        try {
            val expected = download(url, part)
            if (!AudioFileVerifier.looksLikeMp3(part, expected)) {
                part.delete()
                return@withContext Result.failure()
            }
            if (target.exists()) target.delete()
            if (!part.renameTo(target)) throw IOException("Could not finalise download")
            reciterDao.setLocalFile(reciterId, target.absolutePath)
            Result.success()
        } catch (e: CancellationException) {
            throw e // paused / cancelled: keep the partial file
        } catch (e: IOException) {
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    /** Returns the expected total size in bytes, or -1 if the server did not report it. */
    private suspend fun download(url: String, part: java.io.File): Long {
        val existing = if (part.exists()) part.length() else 0L
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 20_000
            if (existing > 0) setRequestProperty("Range", "bytes=$existing-")
        }
        try {
            val code = connection.responseCode
            if (code == HTTP_RANGE_NOT_SATISFIABLE && existing > 0) return existing // already complete
            if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_PARTIAL) {
                throw IOException("HTTP $code")
            }
            val append = code == HttpURLConnection.HTTP_PARTIAL
            val remaining = connection.contentLengthLong
            val total = if (remaining < 0) -1L else if (append) existing + remaining else remaining
            var written = if (append) existing else 0L
            var lastReported = 0L

            connection.inputStream.use { input ->
                FileOutputStream(part, append).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        written += read
                        if (written - lastReported >= PROGRESS_STEP_BYTES) {
                            setProgress(workDataOf(KEY_BYTES to written, KEY_TOTAL to total))
                            lastReported = written
                        }
                    }
                }
            }
            if (total > 0 && written != total) throw IOException("Incomplete download $written/$total")
            return total
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val KEY_RECITER_ID = "reciter_id"
        const val KEY_URL = "url"
        const val KEY_BYTES = "bytes"
        const val KEY_TOTAL = "total"
        private const val MAX_ATTEMPTS = 5
        private const val BUFFER_SIZE = 64 * 1024
        private const val PROGRESS_STEP_BYTES = 256 * 1024L
        private const val HTTP_RANGE_NOT_SATISFIABLE = 416
    }
}
