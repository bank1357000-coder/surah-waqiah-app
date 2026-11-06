package com.alwaqiah.quran.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * NOT EXECUTED. Requires app/schemas/.../1.json and 2.json, which Room writes on the first build.
 * Because v1 was never built, generate 1.json first by building with VERSION = 1 (see docs/PHASES.md).
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), WaqiahDatabase::class.java)

    @Test
    fun migrate1To2KeepsBookmarks() {
        helper.createDatabase(DB, 1).apply {
            execSQL("INSERT INTO bookmarks (verseNumber, createdAt, note) VALUES (12, 1, NULL)")
            close()
        }
        val db = helper.runMigrationsAndValidate(DB, 2, true, Migrations.MIGRATION_1_2)
        db.query("SELECT COUNT(*) FROM bookmarks").use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM memorization_activity").use { c ->
            c.moveToFirst()
            assertEquals(0, c.getInt(0))
        }
    }

    private companion object { const val DB = "migration-test" }
}
