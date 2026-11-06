# PRD — Surah Al-Waqiah Native Android App

## Original problem statement
Hand-written native Android Studio project (Kotlin, Jetpack Compose, Material 3, Gradle KTS, Room, Media3, MVVM + Clean
Architecture, Hilt, DataStore, Coroutines/Flow, Navigation Compose, WorkManager) dedicated to Surah Al-Waqiah:
verified Uthman Taha text, Persian/English translations, advanced audio player with verified timestamps, memorization
mode, tafsir, surah info & virtues, bookmarks, search, settings, about/contact/other apps/share. No Expo/RN/web.
Delivered in phases; never compiled here; be transparent.

## User choices
- Persian-only RTL UI
- Persian translations: Makarem Shirazi (default), Fooladvand, Ansarian; English: Saheeh International (default)
- App name "سوره واقعه", package `com.alwaqiah.quran` (default, changeable)

## Location
`/app/android/WaqiahQuran` (Android Studio root). Expo template in /app/frontend untouched/unused.

## Implemented — Phase 1 (June 2026, NOT compiled)
- Gradle KTS + version catalog (AGP 8.7.3, Kotlin 2.1.0, Gradle 8.11.1, JDK 17), wrapper jar/scripts
- Room v1 (11 tables), DataStore prefs, Hilt modules, ContentSeeder (versioned, idempotent)
- Verified content: KFGQPC Hafs + Tanzil Uthmani text, 4 translations, 5 reciters × 96 Quran.com timings
- Fonts: KFGQPC Uthmanic Hafs, Amiri Quran, Vazirmatn
- Screens: Home, Reader (select/bookmark/copy/share/history), Bookmarks, Settings (all prefs), Surah Info,
  About, Contact, Other apps, Share app; coming-soon screens for Player, Memorization, Tafsir, Search, Virtues
- Media3 PlaybackService foundation; FindVerseAtPositionUseCase
- Tests written (not run): 4 unit, 1 instrumented

## Backlog
- P0 Phase 2: full audio player + verse sync + repeat + downloads
- P1 Phase 3: memorization; Phase 4: tafsir + search
- P2 Phase 5: surah intro & virtues (sourced); Phase 6: polish/a11y/baseline profile
