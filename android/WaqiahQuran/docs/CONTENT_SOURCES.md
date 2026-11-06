# Content sources, provenance and licensing

**No Quranic text, translation or timestamp in this project was typed or generated from memory.**
All content was downloaded programmatically from the sources below (June 2026) and written into
`app/src/main/assets/quran/` by a script, then validated (counts, ordering, monotonic timings).

## Arabic text

| Field | Source | Notes |
|---|---|---|
| `textHafs` | King Fahd Glorious Qur'an Printing Complex (KFGQPC) Hafs text, via `https://api.quran.com/api/v4/quran/verses/qpc_hafs?chapter_number=56` | Encoded for the KFGQPC Uthmanic Script HAFS font (Uthman Taha calligraphy). The trailing verse-number glyph was stripped; numbers are drawn by the app. |
| `textTanzil` | Tanzil Project Uthmani text, via `https://api.alquran.cloud/v1/surah/56/editions/quran-uthmani` | The Bismillah prefix on verse 1 was moved to `bismillahTanzil`. Tanzil licence (CC BY 3.0 terms): text may be used/redistributed verbatim with attribution to tanzil.net; must not be modified. |
| `bismillahHafs` | KFGQPC text of verse 1:1 via Quran.com API | Shown as header only; not counted as a verse of Surah 56. |
| `juz`, `page` | Tanzil metadata via alquran.cloud | Madani mushaf page numbers. |

## Translations (Tanzil translations via alquran.cloud)

| id | Translator |
|---|---|
| `fa.makarem` | Naser Makarem Shirazi (default) |
| `fa.fooladvand` | Mohammad Mahdi Fooladvand |
| `fa.ansarian` | Hussain Ansarian |
| `en.sahih` | Saheeh International |

Orthography: in the Persian translations, Arabic Yeh (U+064A) and Kaf (U+0643) were normalised to Persian Yeh
(U+06CC) and Keheh (U+06A9) so all three translations are consistent and searchable. The Arabic Quran text is untouched.

Tanzil translations are provided for non-commercial use with attribution; check each translator's terms before any
commercial release.

## Audio and verse timestamps

From the Quran Foundation / Quran.com QDC API:
`https://api.qurancdn.com/api/qdc/audio/reciters/{id}/audio_files?chapter=56&segments=true`

| id | Reciter | Audio URL |
|---|---|---|
| 7 | Mishari Rashid al-Afasy | download.quranicaudio.com/qdc/mishari_al_afasy/murattal/56.mp3 |
| 2 | AbdulBaset AbdulSamad (Murattal) | download.quranicaudio.com/qdc/abdul_baset/murattal/56.mp3 |
| 6 | Mahmoud Khalil Al-Husary | download.quranicaudio.com/qdc/khalil_al_husary/murattal/56.mp3 |
| 9 | Mohamed Siddiq al-Minshawi | download.quranicaudio.com/qdc/siddiq_minshawi/murattal/56.mp3 |
| 3 | Abdur-Rahman as-Sudais | download.quranicaudio.com/qdc/abdurrahmaan_as_sudais/murattal/56.mp3 |

`fromMs`/`toMs` are the API's `timestamp_from`/`timestamp_to` for each verse, unchanged. Audio is streamed from the
CDN and never stored in Room. Confirm QuranicAudio / Quran Foundation terms before publishing.
Word-level segments exist in the API and can be imported later for word highlighting.

## Fonts

| File | Font | Licence |
|---|---|---|
| `uthmanic_hafs.ttf` | KFGQPC Uthmanic Script HAFS v18 | © King Fahd Complex — free to use, must not be modified or sold. Downloaded from the Quran.com open-source frontend repository. |
| `amiri_quran.ttf` | Amiri Quran 1.000 | SIL OFL 1.1 (`docs/licenses/Amiri-OFL.txt`) |
| `vazirmatn_*.ttf` | Vazirmatn 33.003 | SIL OFL 1.1 (`docs/licenses/Vazirmatn-OFL.txt`) |

## Not yet included (deliberately)

- **Tafsir** (Phase 4) and **virtues narrations** (Phase 5) will only be added from named, verifiable sources with
  per-item attribution, and narrations will be labelled verified vs disputed. Nothing has been written for them yet.
