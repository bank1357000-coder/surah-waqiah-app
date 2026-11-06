package com.alwaqiah.quran.audio

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player

/**
 * Player handed to the MediaSession. A surah is one audio file, so "next/previous" from the
 * notification, lock screen, headset or Bluetooth are re-mapped to next/previous VERSE.
 */
class VerseAwarePlayer(
    player: Player,
    private val onNextVerse: () -> Unit,
    private val onPreviousVerse: () -> Unit,
    private val onPlayRequested: () -> Unit,
    private val onPauseRequested: () -> Unit,
) : ForwardingPlayer(player) {

    override fun getAvailableCommands(): Player.Commands =
        super.getAvailableCommands().buildUpon()
            .addAll(
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            )
            .build()

    override fun isCommandAvailable(command: Int): Boolean = availableCommands.contains(command)

    override fun seekToNext() = onNextVerse()
    override fun seekToNextMediaItem() = onNextVerse()
    override fun seekToPrevious() = onPreviousVerse()
    override fun seekToPreviousMediaItem() = onPreviousVerse()

    override fun play() {
        onPlayRequested()
        super.play()
    }

    override fun pause() {
        onPauseRequested()
        super.pause()
    }
}
