# Architecture

MVVM + Clean Architecture in one `:app` module with strict package layering
(presentation → domain ← data). Splitting into Gradle modules (`:core`, `:data`, `:audio`, `:feature-*`) later
is straightforward because only `domain` interfaces cross layer boundaries.

## Data flow

```
assets/quran/*.json ──ContentSeeder (first launch, idempotent, transaction)──► Room (waqiah.db)
                                                                                 │ Flow
DataStore (user_preferences) ──► UserPreferencesRepository ─┐                    ▼
                                                            ├─► ViewModel (combine/flatMapLatest → StateFlow UiState)
Room DAOs ──► Repository implementations ───────────────────┘                    │ collectAsStateWithLifecycle
                                                                                 ▼
                                                                          Compose screens
```

- Every content repository waits for `ContentSeeder.ensureSeeded()` before reading, so the app works offline from the
  very first launch. The seeder also runs at app start on a background scope.
- ViewModels expose a single `StateFlow` with `SharingStarted.WhileSubscribed(5_000)`: collection stops in background
  and survives configuration changes.
- The reader only re-queries translations when the selected edition changes (`distinctUntilChanged` key), not on
  font-size tweaks. Lists use stable keys and `contentType` to minimise recomposition.

## Room schema (v1)

`surah`, `verses`, `translation_editions`, `verse_translations`, `tafsir_sources` (metadata only), `reciters`
(CDN URL + optional local file path), `verse_timings`, `bookmarks` (unique per verse), `notes`,
`memorization_progress`, `reading_history` (pruned to the latest 200 rows).

Schemas are exported to `app/schemas/`. Destructive migration is **disabled**; see `data/local/Migrations.kt`.

## Audio engine (Phase 1 foundation → Phase 2)

`audio/PlaybackService` is a Media3 `MediaSessionService` with ExoPlayer configured for speech, audio-focus handling,
becoming-noisy pause and a network wake lock. That gives background playback, the media notification and
lock-screen / headset controls. Phase 2 adds a `PlaybackController` (MediaController wrapper exposed as Flow),
position polling → `FindVerseAtPositionUseCase` for verse highlighting/auto-scroll, clipping-based verse/range repeat
with pauses, speed, offline files via WorkManager downloads, and state restoration after interruptions.
