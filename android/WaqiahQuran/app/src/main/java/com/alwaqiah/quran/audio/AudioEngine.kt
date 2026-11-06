package com.alwaqiah.quran.audio

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import androidx.media3.session.MediaSession
import com.alwaqiah.quran.R
import com.alwaqiah.quran.domain.model.Reciter
import com.alwaqiah.quran.domain.model.VerseTiming
import com.alwaqiah.quran.domain.playback.PlaybackError
import com.alwaqiah.quran.domain.playback.PlaybackSpeed
import com.alwaqiah.quran.domain.playback.PlaybackState
import com.alwaqiah.quran.domain.playback.RepeatDecision
import com.alwaqiah.quran.domain.playback.RepeatEngine
import com.alwaqiah.quran.domain.playback.RepeatSettings
import com.alwaqiah.quran.domain.repository.ReciterRepository
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import com.alwaqiah.quran.domain.usecase.FindVerseAtPositionUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single owner of the app's ExoPlayer (one player per process — no duplicates).
 *
 * Verse synchronisation: each reciter has ONE full-surah file plus verified verse timestamps.
 * Instead of polling, a Media3 [PlayerMessage] is scheduled at the current verse's end timestamp;
 * ExoPlayer delivers it precisely on the playback timeline (also in background). Each message
 * carries a generation token; any seek/reload bumps the generation so stale messages are dropped.
 *
 * Threading: every method must be called on the main thread (ExoPlayer requirement).
 */
@Singleton
class AudioEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reciterRepository: ReciterRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val connection: PlaybackConnection,
    private val findVerse: FindVerseAtPositionUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repeat = RepeatEngine(RepeatSettings.LAST_VERSE)
    private val loadMutex = Mutex()

    private var exo: ExoPlayer? = null
    private var sessionPlayer: VerseAwarePlayer? = null
    private var loadedReciter: Reciter? = null
    private var timings: List<VerseTiming> = emptyList()
    private var repeatConfigured = false

    private var boundaryMessage: PlayerMessage? = null
    private var generation = 0
    private var gapJob: Job? = null
    private var internalSeek = false
    private var internalLoad = false
    private var networkCallbackRegistered = false

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    /** Current position, emitted only while collected (UI). Background sync does not depend on it. */
    fun positionUpdates(intervalMs: Long = 250L): Flow<Long> = flow {
        while (true) {
            emit(exo?.currentPosition ?: 0L)
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.Main.immediate)

    // region Session / lifecycle

    fun sessionPlayer(): Player {
        sessionPlayer?.let { return it }
        val player = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true, // pauses for calls / other apps, resumes after transient loss
            )
            .setHandleAudioBecomingNoisy(true) // headphones unplugged → pause
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .build()
        player.addListener(listener)
        exo = player
        registerNetworkRecovery()
        return VerseAwarePlayer(
            player,
            onNextVerse = ::nextVerse,
            onPreviousVerse = ::previousVerse,
            onPlayRequested = ::onExternalPlay,
            onPauseRequested = ::cancelGap,
        ).also { sessionPlayer = it }
    }

    /** Called by the service when it is destroyed. */
    fun release() {
        cancelGap()
        boundaryMessage?.cancel()
        boundaryMessage = null
        exo?.removeListener(listener)
        exo?.release()
        exo = null
        sessionPlayer = null
        loadedReciter = null
        timings = emptyList()
        _state.update { PlaybackState(speed = it.speed, repeat = it.repeat, currentVerse = it.currentVerse) }
    }

    // endregion

    // region Public commands (suspend ones also make sure the service is bound)

    suspend fun prepare(reciterId: Int? = null) {
        connection.ensureConnected()
        ensureLoaded(reciterId)
    }

    suspend fun playFrom(verse: Int) {
        prepare()
        val start = repeat.start(verse)
        moveToVerse(start, manual = false)
        _state.update { it.copy(isFinished = false) }
        exo?.play()
    }

    suspend fun togglePlayPause() {
        prepare()
        val player = exo ?: return
        when {
            gapJob != null -> { cancelGap(); player.play() }
            _state.value.isFinished -> playFrom(_state.value.activeRange.first)
            player.playWhenReady -> player.pause()
            else -> {
                if (_state.value.error != null) retry()
                player.play()
            }
        }
    }

    suspend fun changeReciter(reciterId: Int) {
        preferencesRepository.setReciter(reciterId)
        prepare(reciterId)
    }

    fun nextVerse() {
        val next = _state.value.currentVerse + 1
        if (timings.isEmpty() || next > RepeatSettings.LAST_VERSE) return
        moveToVerse(next, manual = true)
    }

    /** Restarts the current verse if more than 2 s in, otherwise goes to the previous verse. */
    fun previousVerse() {
        val player = exo ?: return
        if (timings.isEmpty()) return
        val current = _state.value.currentVerse
        val intoVerse = player.currentPosition - timings[current - 1].fromMs
        moveToVerse(if (intoVerse > RESTART_THRESHOLD_MS || current == 1) current else current - 1, manual = true)
    }

    fun seekBy(deltaMs: Long) = seekTo((exo?.currentPosition ?: 0L) + deltaMs)

    fun seekTo(positionMs: Long) {
        val player = exo ?: return
        val duration = player.duration.takeIf { it != C.TIME_UNSET } ?: Long.MAX_VALUE
        val target = positionMs.coerceIn(0L, duration)
        seekInternal(target)
        onUserPositionChange(target)
    }

    fun setSpeed(speed: Float) {
        val value = PlaybackSpeed.clamp(speed)
        exo?.setPlaybackSpeed(value) // applied in place, no re-buffering
        _state.update { it.copy(speed = value) }
        scope.launch { preferencesRepository.setPlaybackSpeed(value) }
    }

    fun setRepeatSettings(settings: RepeatSettings) {
        repeat.configure(settings, _state.value.currentVerse)
        repeatConfigured = true
        if (timings.isNotEmpty()) scheduleBoundary(repeat.currentVerse)
        _state.update { it.copy(isFinished = false, currentVerse = repeat.currentVerse) }
        publishRepeatState()
        scope.launch { preferencesRepository.setRepeatSettings(repeat.settings) }
    }

    fun retry() {
        val player = exo ?: return
        _state.update { it.copy(error = null) }
        player.prepare()
    }

    /** Used by MediaSession playback resumption (e.g. Bluetooth "play" after the app was killed). */
    fun launchResumption(callback: (Result<MediaSession.MediaItemsWithStartPosition>) -> Unit) {
        scope.launch {
            callback(runCatching {
                val prefs = preferencesRepository.preferences.first()
                val reciter = findReciter(prefs.reciterId)
                val verseTimings = reciterRepository.getTimings(reciter.id)
                val verse = prefs.lastPlayedVerse.coerceIn(1, verseTimings.size)
                MediaSession.MediaItemsWithStartPosition(
                    listOf(buildMediaItem(reciter, localFileOf(reciter))),
                    0,
                    verseTimings[verse - 1].fromMs,
                )
            })
        }
    }

    // endregion

    // region Loading

    private suspend fun ensureLoaded(requestedReciter: Int?) = loadMutex.withLock {
        val prefs = preferencesRepository.preferences.first()
        val id = requestedReciter ?: loadedReciter?.id ?: prefs.reciterId
        val player = sessionPlayer().let { exo!! }
        if (loadedReciter?.id == id && player.mediaItemCount > 0) return@withLock

        val reciter = findReciter(id)
        val newTimings = reciterRepository.getTimings(id)
        check(newTimings.size == RepeatSettings.LAST_VERSE) { "Incomplete timings for reciter $id" }

        val verse = (if (loadedReciter != null) _state.value.currentVerse else prefs.lastPlayedVerse)
            .coerceIn(1, RepeatSettings.LAST_VERSE)
        val local = localFileOf(reciter)

        if (!repeatConfigured) {
            repeat.configure(prefs.toRepeatSettings(), verse)
            repeatConfigured = true
        }

        cancelGap()
        timings = newTimings // never mix timings between reciters: swapped together with the source
        loadedReciter = reciter
        internalLoad = true
        try {
            player.setMediaItem(buildMediaItem(reciter, local), newTimings[verse - 1].fromMs)
        } finally {
            internalLoad = false
        }
        player.setPlaybackSpeed(prefs.playbackSpeed)
        player.prepare()
        repeat.onManualSeek(verse)
        scheduleBoundary(verse)

        _state.update {
            it.copy(
                isLoaded = true,
                reciterId = reciter.id,
                reciterName = reciter.nameFa,
                isOfflineSource = local != null,
                currentVerse = verse,
                speed = prefs.playbackSpeed,
                error = null,
                isFinished = false,
            )
        }
        publishRepeatState()
    }

    /** A MediaItem was set from outside the engine (playback resumption): adopt its timings. */
    private fun adoptExternalItem(reciterId: Int) {
        scope.launch {
            loadMutex.withLock {
                val reciter = findReciter(reciterId)
                timings = reciterRepository.getTimings(reciterId)
                loadedReciter = reciter
                if (!repeatConfigured) {
                    repeat.configure(preferencesRepository.preferences.first().toRepeatSettings(), 1)
                    repeatConfigured = true
                }
                _state.update { it.copy(isLoaded = true, reciterId = reciter.id, reciterName = reciter.nameFa) }
            }
            onUserPositionChange(exo?.currentPosition ?: 0L)
        }
    }

    private suspend fun findReciter(id: Int): Reciter =
        reciterRepository.observeReciters().first().firstOrNull { it.id == id }
            ?: error("Unknown reciter $id")

    private fun localFileOf(reciter: Reciter): File? =
        reciter.localFilePath?.let(::File)?.takeIf { it.isFile && it.length() > 0 }

    private fun buildMediaItem(reciter: Reciter, local: File?): MediaItem =
        MediaItem.Builder()
            .setMediaId("$MEDIA_ID_PREFIX${reciter.id}")
            .setUri(local?.let { Uri.fromFile(it) } ?: Uri.parse(reciter.audioUrl))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(R.string.app_name))
                    .setArtist(reciter.nameFa)
                    .setIsPlayable(true)
                    .build()
            )
            .build()

    // endregion

    // region Verse boundaries & repeat engine

    private fun scheduleBoundary(verse: Int) {
        val player = exo ?: return
        boundaryMessage?.cancel()
        val token = ++generation
        val end = timings.getOrNull(verse - 1)?.toMs ?: return
        boundaryMessage = player.createMessage { _, payload -> onBoundary(verse, payload as Int) }
            .setLooper(Looper.getMainLooper())
            .setPosition(end)
            .setPayload(token)
            .setDeleteAfterDelivery(true)
            .send()
    }

    private fun onBoundary(verse: Int, token: Int) {
        if (token != generation) return // stale message from before a seek/reload
        repeat.onVerseCompleted(verse)?.let(::applyDecision)
    }

    private fun applyDecision(decision: RepeatDecision) {
        when (decision) {
            is RepeatDecision.Advance -> {
                setCurrentVerse(decision.verse)
                scheduleBoundary(decision.verse)
            }
            is RepeatDecision.Restart -> restartAt(decision.verse, decision.pauseMs)
            RepeatDecision.Finish -> finish()
        }
        publishRepeatState()
    }

    private fun restartAt(verse: Int, pauseMs: Long) {
        val player = exo ?: return
        cancelGap()
        val shouldResume = player.playWhenReady || player.playbackState == Player.STATE_ENDED
        if (pauseMs > 0 && shouldResume) player.pause() // pause BEFORE seeking so no audio leaks
        seekInternal(timings[verse - 1].fromMs)
        setCurrentVerse(verse)
        scheduleBoundary(verse)
        if (pauseMs > 0 && shouldResume) {
            _state.update { it.copy(isInRepeatPause = true) }
            gapJob = scope.launch {
                delay(pauseMs)
                gapJob = null
                _state.update { it.copy(isInRepeatPause = false) }
                exo?.play()
            }
        } else if (shouldResume) {
            player.play()
        }
    }

    private fun finish() {
        cancelGap()
        boundaryMessage?.cancel()
        generation++
        exo?.pause()
        _state.update { it.copy(isFinished = true) }
    }

    private fun moveToVerse(verse: Int, manual: Boolean) {
        if (timings.isEmpty()) return
        cancelGap()
        seekInternal(timings[verse - 1].fromMs)
        if (manual) repeat.onManualSeek(verse)
        setCurrentVerse(verse)
        scheduleBoundary(verse)
        _state.update { it.copy(isFinished = false) }
        publishRepeatState()
    }

    private fun onUserPositionChange(positionMs: Long) {
        val verse = findVerse(timings, positionMs) ?: return
        cancelGap()
        repeat.onManualSeek(verse)
        setCurrentVerse(verse)
        scheduleBoundary(verse)
        _state.update { it.copy(isFinished = false) }
        publishRepeatState()
    }

    private fun onExternalPlay() {
        cancelGap()
        if (_state.value.isFinished) {
            val start = repeat.start(_state.value.activeRange.first)
            moveToVerse(start, manual = false)
        }
    }

    private fun cancelGap() {
        gapJob?.cancel()
        gapJob = null
        if (_state.value.isInRepeatPause) _state.update { it.copy(isInRepeatPause = false) }
    }

    private fun seekInternal(positionMs: Long) {
        val player = exo ?: return
        internalSeek = true
        try {
            player.seekTo(positionMs)
        } finally {
            internalSeek = false
        }
    }

    private fun setCurrentVerse(verse: Int) {
        if (_state.value.currentVerse == verse) return
        _state.update { it.copy(currentVerse = verse) }
        scope.launch { preferencesRepository.setLastPlayedVerse(verse) }
    }

    private fun publishRepeatState() {
        _state.update {
            it.copy(
                repeat = repeat.settings,
                versePlays = repeat.versePlays,
                rangePlays = repeat.rangePlays,
                activeRange = repeat.activeRange,
            )
        }
    }

    // endregion

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val duration = exo?.duration?.takeIf { it != C.TIME_UNSET }
            _state.update {
                it.copy(isBuffering = playbackState == Player.STATE_BUFFERING, durationMs = duration ?: it.durationMs)
            }
            // The last verse may end exactly at the end of the file, where the boundary message can't fire.
            if (playbackState == Player.STATE_ENDED && !_state.value.isFinished) {
                boundaryMessage?.cancel()
                generation++
                val decision = repeat.onVerseCompleted(_state.value.currentVerse)
                if (decision == null) finish() else applyDecision(decision)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val kind = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> PlaybackError.NETWORK
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                PlaybackException.ERROR_CODE_DECODING_FAILED -> PlaybackError.SOURCE
                else -> PlaybackError.UNKNOWN
            }
            _state.update { it.copy(error = kind, isBuffering = false) }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            _state.update { it.copy(speed = playbackParameters.speed) }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            // Seeks from the notification / lock screen / external controllers.
            if (reason == Player.DISCONTINUITY_REASON_SEEK && !internalSeek) {
                onUserPositionChange(newPosition.positionMs)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (internalLoad) return
            val id = mediaItem?.mediaId?.removePrefix(MEDIA_ID_PREFIX)?.toIntOrNull() ?: return
            adoptExternalItem(id)
        }
    }

    /** Automatic recovery: when connectivity returns after a network error, re-prepare. */
    private fun registerNetworkRecovery() {
        if (networkCallbackRegistered) return
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { if (_state.value.error == PlaybackError.NETWORK) retry() }
            }
        })
        networkCallbackRegistered = true
    }

    private companion object {
        const val MEDIA_ID_PREFIX = "reciter:"
        const val SEEK_INCREMENT_MS = 10_000L
        const val RESTART_THRESHOLD_MS = 2_000L
    }
}
