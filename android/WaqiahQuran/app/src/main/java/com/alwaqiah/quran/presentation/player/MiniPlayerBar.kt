package com.alwaqiah.quran.presentation.player

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.playback.PlaybackState

/** Compact player shown at the bottom of the reader while audio is loaded. */
@Composable
fun MiniPlayerBar(
    playback: PlaybackState,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onOpen,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth().testTag("mini-player"),
    ) {
        Row(Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.mini_player_verse, playback.currentVerse.toPersianDigits(), playback.reciterName),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                modifier = Modifier.weight(1f).testTag("mini-player-verse"),
            )
            IconButton(onClick = onToggle, modifier = Modifier.testTag("mini-player-play-pause")) {
                val playing = playback.isPlaying || playback.isInRepeatPause
                Icon(
                    if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (playing) R.string.player_pause else R.string.player_play),
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.testTag("mini-player-next")) {
                Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.player_next_verse))
            }
        }
    }
}
