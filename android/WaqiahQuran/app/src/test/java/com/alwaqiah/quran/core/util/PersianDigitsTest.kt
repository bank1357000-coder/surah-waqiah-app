package com.alwaqiah.quran.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PersianDigitsTest {
    @Test fun convertsAllDigits() = assertEquals("۰۱۲۳۴۵۶۷۸۹", "0123456789".toPersianDigits())
    @Test fun keepsNonDigits() = assertEquals("آیه ۹۶", "آیه 96".toPersianDigits())
    @Test fun convertsInt() = assertEquals("۵۶", 56.toPersianDigits())
}
