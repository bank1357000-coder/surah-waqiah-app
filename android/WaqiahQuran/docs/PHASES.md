# Delivery phases

| Phase | Scope | Status |
|---|---|---|
| 1 | Gradle/Kotlin DSL setup, version catalog, wrapper, manifest, Hilt, Room + migrations scaffold, DataStore, verified content import, theme (light/dark/system, Persian RTL), navigation for all 14 screens, Home, Reader (select, bookmark, copy, share, reading history, font size, script, translations), Bookmarks, Settings (all preferences), Surah Info (metadata facts), About, Contact, Other apps, Share app, Media3 service foundation, tests | **Written, not compiled** |
| 2 | Audio player: controller, streaming, reciter switch, play/pause/prev/next verse, speed 1.0–2.0 with step, verse/range/surah/unlimited repeat with pause, timestamp highlighting + auto-scroll in reader, offline downloads (WorkManager), interruption recovery | Planned |
| 3 | Memorization: verse/range repetition counts, hide/reveal text, pauses, progress tracking, daily goals (Room migration v1→v2) | Planned |
| 4 | Tafsir (verified Persian/English sources with attribution, expandable sections, audio tafsir sync) + Search (Arabic with diacritic-insensitive matching, translations, tafsir) | Planned |
| 5 | Surah introduction, historical/thematic info and virtues — sourced, verified vs disputed narrations clearly separated; notes UI; notification preferences wiring | Planned |
| 6 | Polish: accessibility audit, baseline profile, more UI tests, release config | Planned |

## Phase 1 — files

- Root: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`,
  `gradle/wrapper/*`, `gradlew`, `gradlew.bat`, `README.md`, `docs/*`
- App: `app/build.gradle.kts`, `proguard-rules.pro`, `AndroidManifest.xml`, `res/*` (strings, themes, colours,
  adaptive icon, 5 fonts), `assets/quran/surah_56.json`, `assets/quran/reciters.json`
- Kotlin: see package tree in README.

## Phase 1 — local testing checklist

1. App launches with splash, then the Home screen in Persian RTL.
2. Reader shows Bismillah + 96 verses in the KFGQPC Hafs font with the Makarem translation.
3. Tap a verse → highlight + bookmark / copy / share actions. Bookmark icon appears; it is listed in Bookmarks.
4. Scroll, leave and return to Home → "continue from verse N" appears.
5. Settings: switch theme, font size (preview updates), script, translation, show English — reader updates.
6. Kill and relaunch the app in airplane mode → everything above still works (offline).
