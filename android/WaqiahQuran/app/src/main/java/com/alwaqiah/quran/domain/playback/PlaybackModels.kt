package com.alwaqiah.quran.domain.playback

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/** Playback speed rules: 1.0x–2.0x, stepping by a user-chosen increment and snapping to the grid. */
object PlaybackSpeed {
    const val MIN = 1.0f
    const val MAX = 2.0f
    val STEPS = listOf(0.05f, 0.1f, 0.25f)
    val PRESETS = listOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    fun clamp(value: Float): Float = ((value * 100f).roundToInt() / 100f).coerceIn(MIN, MAX)

    /** Moves one [step] up (direction > 0) or down, snapping off-grid values to the grid first. */
    fun step(current: Float, step: Float, direction: Int): Float {
        val units = (current - MIN) / step
        val target = if (direction > 0) floor(units + EPS) + 1f else ceil(units - EPS) - 1f
        return clamp(MIN + target * step)
    }

    private const val EPS = 1e-3f
}

enum class PlaybackError { NETWORK, SOURCE, UNKNOWN }

/** Snapshot of the audio engine exposed to the UI. Position is published separately (see AudioEngine). */
data class PlaybackState(
    val isLoaded: Boolean = false,
    val reciterId: Int? = null,
    val reciterName: String = "",
    val isOfflineSource: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isInRepeatPause: Boolean = false,
    val isFinished: Boolean = false,
    val currentVerse: Int = 1,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    val repeat: RepeatSettings = RepeatSettings(),
    val versePlays: Int = 0,
    val rangePlays: Int = 0,
    val activeRange: IntRange = 1..RepeatSettings.LAST_VERSE,
    val error: PlaybackError? = null,
)
