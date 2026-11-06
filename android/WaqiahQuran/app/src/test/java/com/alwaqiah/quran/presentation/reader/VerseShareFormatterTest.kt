package com.alwaqiah.quran.presentation.reader

import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.model.ReaderVerse
import com.alwaqiah.quran.domain.model.Verse
import org.junit.Assert.assertEquals
import org.junit.Test

class VerseShareFormatterTest {
    private val item = ReaderVerse(
        verse = Verse(1, "HAFS", "TANZIL", 27, 534),
        primaryTranslation = "FA",
        englishTranslation = null,
        isBookmarked = false,
    )

    @Test fun hafsWithTranslation() =
        assertEquals("HAFS\n\nFA\n\n(REF)", VerseShareFormatter.format(item, ArabicScript.HAFS_KFGQPC, "REF"))

    @Test fun tanzilWithEnglish() =
        assertEquals(
            "TANZIL\n\nFA\n\nEN\n\n(REF)",
            VerseShareFormatter.format(item.copy(englishTranslation = "EN"), ArabicScript.TANZIL_AMIRI, "REF"),
        )
}
