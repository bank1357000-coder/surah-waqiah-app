package com.alwaqiah.quran.presentation.reader

import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.model.ReaderVerse

/** Builds the plain-text payload used for copy and share. Pure function — unit tested. */
object VerseShareFormatter {
    fun format(item: ReaderVerse, script: ArabicScript, reference: String): String = buildString {
        append(if (script == ArabicScript.HAFS_KFGQPC) item.verse.textHafs else item.verse.textTanzil)
        item.primaryTranslation?.let { append("\n\n").append(it) }
        item.englishTranslation?.let { append("\n\n").append(it) }
        append("\n\n(").append(reference).append(')')
    }
}
