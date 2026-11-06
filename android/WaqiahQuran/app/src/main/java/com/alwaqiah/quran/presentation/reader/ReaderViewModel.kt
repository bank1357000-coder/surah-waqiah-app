package com.alwaqiah.quran.presentation.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.alwaqiah.quran.audio.AudioEngine
import com.alwaqiah.quran.domain.playback.PlaybackState
import com.alwaqiah.quran.domain.model.ReaderVerse
import com.alwaqiah.quran.domain.model.SurahInfo
import com.alwaqiah.quran.domain.model.UserPreferences
import com.alwaqiah.quran.domain.repository.BookmarkRepository
import com.alwaqiah.quran.domain.repository.QuranRepository
import com.alwaqiah.quran.domain.repository.ReadingHistoryRepository
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import com.alwaqiah.quran.presentation.navigation.ReaderRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Error(val message: String) : ReaderUiState
    data class Success(
        val surah: SurahInfo,
        val verses: List<ReaderVerse>,
        val preferences: UserPreferences,
    ) : ReaderUiState
}

private data class TranslationKey(val primary: String, val english: String?)
private data class ReaderContent(val surah: SurahInfo, val verses: List<ReaderVerse>)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quranRepository: QuranRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val historyRepository: ReadingHistoryRepository,
    preferencesRepository: UserPreferencesRepository,
    private val audioEngine: AudioEngine,
) : ViewModel() {

    val playback: StateFlow<PlaybackState> = audioEngine.state

    /** Verse being recited, or null when no audio is loaded. Distinct so only 2 items recompose per change. */
    val activeVerse: StateFlow<Int?> = audioEngine.state
        .map { if (it.isLoaded) it.currentVerse else null }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun playFrom(verse: Int) {
        viewModelScope.launch { runCatching { audioEngine.playFrom(verse) } }
    }

    fun togglePlay() {
        viewModelScope.launch { runCatching { audioEngine.togglePlayPause() } }
    }

    fun nextVerse() = audioEngine.nextVerse()

    /** Verse to scroll to on first load (0 = top of surah). */
    val startVerse: Int = savedStateHandle.toRoute<ReaderRoute>().startVerse

    private val _selectedVerse = MutableStateFlow<Int?>(null)
    val selectedVerse: StateFlow<Int?> = _selectedVerse.asStateFlow()

    private val retry = MutableStateFlow(0)
    private val preferences = preferencesRepository.preferences

    // Only re-query translations when the chosen editions change, not on every preference tweak.
    private val translationKey: Flow<TranslationKey> = preferences
        .map { TranslationKey(it.primaryTranslationId, it.englishTranslationId.takeIf { _ -> it.showEnglishTranslation }) }
        .distinctUntilChanged()

    private val content: Flow<ReaderContent> = translationKey.flatMapLatest { key ->
        combine(
            quranRepository.observeSurah().filterNotNull(),
            quranRepository.observeVerses(),
            quranRepository.observeTranslation(key.primary),
            key.english?.let { quranRepository.observeTranslation(it) } ?: flowOf(emptyMap<Int, String>()),
            bookmarkRepository.observeBookmarkedVerseNumbers(),
        ) { surah, verses, primary, english, bookmarked ->
            ReaderContent(
                surah = surah,
                verses = verses.map { verse ->
                    ReaderVerse(
                        verse = verse,
                        primaryTranslation = primary[verse.number],
                        englishTranslation = english[verse.number],
                        isBookmarked = verse.number in bookmarked,
                    )
                },
            )
        }
    }

    val uiState: StateFlow<ReaderUiState> = retry.flatMapLatest {
        combine(content, preferences) { c, p -> ReaderUiState.Success(c.surah, c.verses, p) as ReaderUiState }
            .onStart { emit(ReaderUiState.Loading) }
            .catch { emit(ReaderUiState.Error(it.message.orEmpty())) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReaderUiState.Loading)

    fun onRetry() = retry.update { it + 1 }

    fun onVerseTapped(number: Int) = _selectedVerse.update { if (it == number) null else number }

    fun onToggleBookmark(number: Int) {
        viewModelScope.launch { bookmarkRepository.toggle(number) }
    }

    fun onVerseVisible(number: Int) {
        viewModelScope.launch { historyRepository.record(number) }
    }
}
