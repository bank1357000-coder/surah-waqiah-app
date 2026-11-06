package com.alwaqiah.quran.domain.model

data class Reciter(
    val id: Int,
    val nameFa: String,
    val nameEn: String,
    val style: String,
    val audioUrl: String,
    val timingSource: String,
    val localFilePath: String?,
)

sealed interface DownloadStatus {
    data object NotDownloaded : DownloadStatus
    data object Queued : DownloadStatus
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : DownloadStatus {
        val fraction: Float? get() = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else null
    }
    data class Paused(val downloadedBytes: Long) : DownloadStatus
    data class Downloaded(val sizeBytes: Long) : DownloadStatus
    data object Failed : DownloadStatus
}

/** Verse-level timestamp inside a reciter's full-surah audio file (milliseconds). */
data class VerseTiming(
    val verse: Int,
    val fromMs: Long,
    val toMs: Long,
)
