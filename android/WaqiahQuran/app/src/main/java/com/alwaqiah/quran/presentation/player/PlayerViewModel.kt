package com.alwaqiah.quran.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alwaqiah.quran.audio.AudioEngine
import com.alwaqiah.quran.domain.model.DownloadStatus
import com.alwaqiah.quran.domain.model.Reciter
import com.alwaqiah.quran.domain.model.UserPreferences
import com.alwaqiah.quran.domain.model.Verse
import com.alwaqiah.quran.domain.playback.PlaybackSpeed
import com.alwaqiah.quran.domain.playback.PlaybackState
import com.alwaqiah.quran.domain.playback.RepeatSettings
import com.alwaqiah.quran.domain.repository.AudioDownloadRepository
import com.alwaqiah.quran.domain.repository.QuranRepository
import com.alwaqiah.quran.domain.repository.ReciterRepository
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerContent(
    val verses: List<Verse>,
    val translation: Map<Int, String>,
    val reciters: List<Reciter>,
    val preferences: UserPreferences,
    val downloads: Map<Int, DownloadStatus>,
    val storageBytes: Long,
)

enum class PlayerEvent { COMMAND_FAILED }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val engine: AudioEngine,
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val downloads: AudioDownloadRepository,
) : ViewModel() {

    val playback: StateFlow<PlaybackState> = engine.state

    /** Collected only by the progress composable, so the rest of the screen does not recompose 4×/s. */
    val position: StateFlow<Long> = engine.positionUpdates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), 0L)

    private val _events = MutableSharedFlow<PlayerEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<PlayerEvent> = _events.asSharedFlow()

    private val translation = preferencesRepository.preferences
        .map { it.primaryTranslationId }
        .distinctUntilChanged()
        .flatMapLatest { quranRepository.observeTranslation(it) }

    val content: StateFlow<PlayerContent?> = combine(
        combine(quranRepository.observeVerses(), translation) { v, t -> v to t },
        reciterRepository.observeReciters(),
        preferencesRepository.preferences,
        downloads.observeStatuses(),
        downloads.observeStorageUsageBytes(),
    ) { (verses, tr), reciters, prefs, statuses, bytes ->
        PlayerContent(verses, tr, reciters, prefs, statuses, bytes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        command { engine.prepare() }
    }

    private fun command(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.tryEmit(PlayerEvent.COMMAND_FAILED)
            }
        }
    }

    fun togglePlay() = command { engine.togglePlayPause() }
    fun playFrom(verse: Int) = command { engine.playFrom(verse) }
    fun next() = engine.nextVerse()
    fun previous() = engine.previousVerse()
    fun seekBy(deltaMs: Long) = engine.seekBy(deltaMs)
    fun seekTo(positionMs: Long) = engine.seekTo(positionMs)
    fun retry() = engine.retry()

    fun setSpeed(speed: Float) = engine.setSpeed(speed)
    fun stepSpeed(direction: Int) {
        val step = content.value?.preferences?.speedStep ?: PlaybackSpeed.STEPS.last()
        engine.setSpeed(PlaybackSpeed.step(playback.value.speed, step, direction))
    }
    fun setSpeedStep(step: Float) = command { preferencesRepository.setSpeedStep(step) }

    fun applyRepeat(settings: RepeatSettings) = engine.setRepeatSettings(settings)

    fun selectReciter(id: Int) = command { engine.changeReciter(id) }
    fun download(id: Int) = command { downloads.start(id) }
    fun pauseDownload(id: Int) = downloads.pause(id)
    fun cancelDownload(id: Int) = command { downloads.cancel(id) }
    fun deleteDownload(id: Int) = command { downloads.delete(id) }
}
