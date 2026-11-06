# سوره واقعه — Surah Al-Waqiah (Native Android)

A native **Kotlin + Jetpack Compose** Android Studio project dedicated to Surah Al-Waqiah (Surah 56, 96 verses).
Persian-only, right-to-left interface.

> ⚠️ **Transparency notice.** This project was hand-written in an environment with **no Android SDK, Gradle, JDK or
> Kotlin compiler**. **Nothing has been compiled, run, or tested.** Expect to fix some compilation errors in Android
> Studio (with Cursor AI). All tests listed below are **NOT EXECUTED**.

## Android Studio root

Open **this folder** (`WaqiahQuran/`, the one containing `settings.gradle.kts`) in Android Studio.

## Toolchain

| Item | Version |
|---|---|
| JDK | **17** (Android Studio's bundled JBR 17/21 works; Gradle JDK = 17+) |
| Android Gradle Plugin | 8.7.3 |
| Gradle (wrapper) | 8.11.1 |
| Kotlin | 2.1.0 (Compose compiler plugin 2.1.0) |
| KSP | 2.1.0-1.0.29 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |
| Android Studio | Ladybug (2024.2.1) or newer |

Libraries (see `gradle/libs.versions.toml`): Compose BOM 2024.12.01 (Material 3), Navigation Compose 2.8.5
(type-safe routes), Hilt 2.54, Room 2.6.1, Media3 1.5.1, DataStore 1.1.1, WorkManager 2.10.0,
kotlinx.serialization 1.7.3, Coroutines 1.9.0.

## First run on Windows

1. Open `WaqiahQuran/` in Android Studio → let Gradle sync (it downloads Gradle 8.11.1 via the wrapper).
2. *Settings → Build Tools → Gradle → Gradle JDK* = 17 or the bundled JBR.
3. Run the `app` configuration on an emulator/device (API 26+).
4. Unit tests: `gradlew.bat testDebugUnitTest`
5. Instrumented tests (device needed): `gradlew.bat connectedDebugAndroidTest`

## Architecture (single `:app` module, layered packages)

```
com.alwaqiah.quran
├── WaqiahApp.kt / MainActivity.kt      Hilt app, WorkManager config, splash, RTL root
├── core/util                           Persian digits, share/email intents
├── domain
│   ├── model                           Pure Kotlin models (Verse, Reciter, UserPreferences…)
│   ├── repository                      Repository interfaces
│   └── usecase                         FindVerseAtPositionUseCase (timestamp → verse)
├── data
│   ├── local (Room)                    WaqiahDatabase, entities, DAOs, Migrations
│   ├── seed                            ContentSeeder: imports verified JSON assets into Room
│   ├── preferences (DataStore)         UserPreferencesRepositoryImpl
│   ├── mapper                          Entity ↔ domain
│   └── repository                      Repository implementations
├── audio                               Media3 PlaybackService (MediaSessionService)
├── di                                  Hilt modules
└── presentation
    ├── theme                           Colours, typography (Vazirmatn / KFGQPC Hafs / Amiri Quran)
    ├── navigation                      Type-safe routes + NavHost
    ├── components / common             Top bar, states, ornaments, coming-soon screen
    └── home, reader, bookmarks, settings, info, about
```

See `docs/ARCHITECTURE.md` for data flow and `docs/CONTENT_SOURCES.md` for content provenance and licences.

## Phase status

See `docs/PHASES.md`. **Phase 1 (this delivery)** = project, Gradle, architecture, Room + DataStore, verified content
import, theme, navigation for every screen, Home, Reader (read / select / bookmark / copy / share / history),
Bookmarks, Settings, Surah Info (facts from metadata), About, Contact, Other apps, Share app, Media3 service foundation.

## Tests (all NOT EXECUTED)

| Test | Type | What it checks |
|---|---|---|
| `PersianDigitsTest` | unit | digit conversion |
| `FindVerseAtPositionUseCaseTest` | unit | timestamp → verse binary search |
| `VerseShareFormatterTest` | unit | copy/share text |
| `AssetIntegrityTest` | unit | 96 verses, 4 translations, 96 monotonic timings per reciter |
| `WaqiahDatabaseTest` | instrumented | Room round-trip, unique bookmarks |

The bundled JSON was validated with Python in the authoring environment (96 verses, 4 × 96 translations,
5 reciters × 96 timings).

## Known issues / to verify locally

- Not compiled: imports, opt-in annotations and API signatures may need small fixes.
- `material-icons-extended` is large; R8 shrinks it in release builds.
- The KFGQPC font is used under the King Fahd Complex terms (free use, no modification) — confirm before publishing.
- `contact_email` in `strings.xml` is a placeholder (`support@example.com`) — replace it.
- Play Store link for "share app" uses the package id `com.alwaqiah.quran` — change `applicationId` if needed.
