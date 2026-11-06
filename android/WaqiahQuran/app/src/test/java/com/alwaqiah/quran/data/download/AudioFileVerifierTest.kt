package com.alwaqiah.quran.data.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AudioFileVerifierTest {
    private fun temp(header: ByteArray, size: Int = 20_000): File =
        File.createTempFile("audio", ".mp3").apply {
            deleteOnExit()
            writeBytes(header + ByteArray(size - header.size))
        }

    @Test fun acceptsId3() = assertTrue(AudioFileVerifier.looksLikeMp3(temp("ID3".toByteArray())))
    @Test fun acceptsFrameSync() = assertTrue(AudioFileVerifier.looksLikeMp3(temp(byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte()))))
    @Test fun rejectsHtmlErrorPage() = assertFalse(AudioFileVerifier.looksLikeMp3(temp("<ht".toByteArray())))
    @Test fun rejectsTinyFile() = assertFalse(AudioFileVerifier.looksLikeMp3(temp("ID3".toByteArray(), size = 100)))
    @Test fun rejectsSizeMismatch() = assertFalse(AudioFileVerifier.looksLikeMp3(temp("ID3".toByteArray()), expectedBytes = 99_999))
}
