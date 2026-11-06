package com.alwaqiah.quran.presentation.memorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alwaqiah.quran.audio.AudioEngine
import com.alwaqiah.quran.domain.memorization.DailyActivity
import com.alwaqiah.quran.domain.memorization.MemorizationProgress
import com.alwaqiah.quran.domain.memorization.MemorizationRepository
import com.alwaqiah.quran.domain.memorization.MemorizationSession
import com.alwaqiah.quran.domain.memorization.MemorizationStats
import com.alwaqiah.quran.domain.memorization.MemorizationStatus
import com.alwaqiah.quran.domain.memorization.MemorizationSummary
import com.alwaqiah.quran.domain.model.UserPreferences
import com.alwaqiah.quran.domain.model.Verse
import com.alwaqiah.quran.domain.playback.PlaybackState
import com.alwaqiah.quran.domain.playback.RepeatScope
import com.alwaqiah.quran.domain.playback.RepeatSettings
import com.alwaqiah.quran.domain.repository.QuranRepository
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemorizationUiState(
    val verses: List<Verse> = emptyList(),
    val translation: Map<Int, String> = emptyMap(),
    val progress: Map<Int, MemorizationProgress> = emptyMap(),
    val summary: MemorizationSummary = MemorizationStats.summarize(emptyMap()),
    val today: DailyActivity = DailyActivity(""),
    val session: MemorizationSession = MemorizationSession(),
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MemorizationViewModel @Inject constructor(
    quranRepository: QuranRepository,
    preferencesRepository: UserPreferencesRepository,
    private val repository: MemorizationRepository,
    private val engine: AudioEngine,
) : ViewModel() {

    val playback: StateFlow<PlaybackState> = engine.state

    private val _sessionActive = MutableStateFlow(false)
    val sessionActive: StateFlow<Boolean> = _sessionActive.asStateFlow()

    /** Verses the user tapped to reveal while text is hidden; cleared when the verse changes. */
    private val _revealed = MutableStateFlow<Set<Int>>(emptySet())
    val revealed: StateFlow<Set<Int>> = _revealed.asStateFlow()

    private val translation = preferencesRepository.preferences.map { it.primaryTranslationId }
        .distinctUntilChanged()
        .flatMapLatest { quranRepository.observeTranslation(it) }

    val uiState: StateFlow<MemorizationUiState> = combine(
        combine(quranRepository.observeVerses(), translation) { v, t -> v to t },
        repository.observeProgress(),
        repository.observeToday(),
        repository.session,
        preferencesRepository.preferences,
    ) { (verses, tr), progress, today, session, prefs ->
        MemorizationUiState(verses, tr, progress, MemorizationStats.summarize(progress, verses.size.coerceAtLeast(1)), today, session, prefs, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemorizationUiState())

    private var trackingJob: Job? = null

    fun updateSession(transform: (MemorizationSession) -> MemorizationSession) {
        viewModelScope.launch { repository.saveSession(transform(uiState.value.session)) }
    }

    /** Starts (or resumes) the session from its saved verse using the shared repeat engine. */
    fun start(fromVerse: Int? = null) {
        viewModelScope.launch {
            val s = repository.session.first()
            val begin = (fromVerse ?: s.lastVerse).coerceIn(s.rangeStart, s.rangeEnd)
            engine.setRepeatSettings(
                RepeatSettings(
                    scope = RepeatScope.RANGE,
                    rangeStart = s.rangeStart,
                    rangeEnd = s.rangeEnd,
                    verseRepeatCount = s.verseRepeatCount,
                    rangeRepeatCount = 1,
                    pauseBetweenRepeatsMs = s.pauseBetweenRepeatsMs,
                ),
            )
            runCatching { engine.playFrom(begin) }
            _sessionActive.value = true
            startTracking()
        }
    }

    /** Practise a single verse (from the review list). */
    fun practiceVerse(verse: Int) {
        updateSession { it.copy(rangeStart = verse, rangeEnd = verse, lastVerse = verse) }
        start(verse)
    }

    fun stop() {
        trackingJob?.cancel()
        _sessionActive.value = false
        viewModelScope.launch { if (engine.state.value.isPlaying) engine.togglePlayPause() }
    }

    fun togglePlay() { viewModelScope.launch { runCatching { engine.togglePlayPause() } } }
    fun next() = engine.nextVerse()
    fun previous() = engine.previousVerse()
    fun reveal(verse: Int) = _revealed.update { it + verse }

    fun mark(verse: Int, status: MemorizationStatus) {
        viewModelScope.launch { repository.setStatus(verse, status) }
    }

    /**
     * Records each completed repetition. A repetition is complete when the per-verse counter grows,
     * or when the verse advances naturally after its last repetition (counter resets to 0).
     */
    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            var last: PlaybackState? = null
            engine.state.collect { now ->
                val prev = last
                last = now
                if (prev == null) return@collect
                val sameVerse = prev.currentVerse == now.currentVerse
                val repeatTarget = now.repeat.verseRepeatCount
                when {
                    sameVerse && now.versePlays > prev.versePlays -> repository.recordRepetition(now.currentVerse)
                    !sameVerse -> {
                        _revealed.value = emptySet()
                        val natural = repeatTarget != RepeatSettings.UNLIMITED && prev.versePlays == repeatTarget - 1
                        if (natural && prev.isPlaying) repository.recordRepetition(prev.currentVerse)
                        repository.saveSession(uiState.value.session.copy(lastVerse = now.currentVerse))
                    }
                    !prev.isFinished && now.isFinished -> {
                        repository.recordRepetition(now.currentVerse)
                        _sessionActive.value = false
                    }
                }
            }
        }
    }
}
