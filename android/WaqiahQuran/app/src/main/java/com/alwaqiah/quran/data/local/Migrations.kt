package com.alwaqiah.quran.data.local

import androidx.room.migration.Migration

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
    val ALL: Array<Migration> = emptyArray()
}
