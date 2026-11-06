package com.alwaqiah.quran.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit Room migrations. User data (bookmarks, notes, memorization, history) must never be
 * dropped, so destructive migration is intentionally NOT enabled.
 *
 * When bumping [WaqiahDatabase.VERSION]:
 * 1. Add a `Migration(from, to)` here and append it to [ALL].
 * 2. Build once so Room exports the new schema JSON into app/schemas/.
 * 3. Add a MigrationTestHelper test in androidTest.
 */
object Migrations {
    /** v2: daily memorization activity. Additive only — existing user data is untouched. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `memorization_activity` (" +
                    "`date` TEXT NOT NULL, `versesPracticed` INTEGER NOT NULL, " +
                    "`repetitions` INTEGER NOT NULL, `newlyMemorized` INTEGER NOT NULL, PRIMARY KEY(`date`))"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
