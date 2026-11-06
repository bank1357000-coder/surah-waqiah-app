package com.alwaqiah.quran.data.download

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Local storage for downloaded surah audio (app-private files dir; never in Room). */
@Singleton
class AudioFileStore @Inject constructor(@ApplicationContext context: Context) {
    private val dir: File = File(context.filesDir, "audio")

    private fun ensureDir(): File = dir.apply { if (!exists()) mkdirs() }

    fun audioFile(reciterId: Int) = File(ensureDir(), "reciter_$reciterId.mp3")
    fun partFile(reciterId: Int) = File(ensureDir(), "reciter_$reciterId.mp3.part")

    /** Total bytes used by complete and partial downloads. Call off the main thread. */
    fun totalBytes(): Long = ensureDir().listFiles()?.sumOf { it.length() } ?: 0L
}

/** Cheap integrity check before a file is marked as available offline. */
object AudioFileVerifier {
    private const val MIN_BYTES = 10 * 1024

    fun looksLikeMp3(file: File, expectedBytes: Long = -1): Boolean {
        if (!file.isFile || file.length() < MIN_BYTES) return false
        if (expectedBytes > 0 && file.length() != expectedBytes) return false
        val header = ByteArray(3)
        val read = file.inputStream().use { it.read(header) }
        if (read < 3) return false
        val id3 = header[0] == 'I'.code.toByte() && header[1] == 'D'.code.toByte() && header[2] == '3'.code.toByte()
        val frameSync = (header[0].toInt() and 0xFF) == 0xFF && (header[1].toInt() and 0xE0) == 0xE0
        return id3 || frameSync
    }
}
