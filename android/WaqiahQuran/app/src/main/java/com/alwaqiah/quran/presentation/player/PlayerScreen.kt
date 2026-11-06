package com.alwaqiah.quran.presentation.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.playback.PlaybackError
import com.alwaqiah.quran.domain.playback.PlaybackState
import com.alwaqiah.quran.domain.playback.RepeatScope
import com.alwaqiah.quran.domain.playback.RepeatSettings
import com.alwaqiah.quran.presentation.components.LoadingState
import com.alwaqiah.quran.presentation.components.VerseNumberBadge
import com.alwaqiah.quran.presentation.components.WaqiahTopBar
import com.alwaqiah.quran.presentation.components.formatDuration
import com.alwaqiah.quran.presentation.components.formatSpeed
import com.alwaqiah.quran.presentation.theme.WaqiahThemeExtras
import com.alwaqiah.quran.presentation.theme.fontFamily
import kotlinx.coroutines.flow.StateFlow

private enum class PlayerSheet { NONE, SPEED, REPEAT, RECITER }

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onOpenReader: (Int) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var sheet by rememberSaveable { mutableStateOf(PlayerSheet.NONE) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbar.showSnackbar(context.getString(R.string.player_error_unknown)) }
    }

    Scaffold(
        topBar = {
            WaqiahTopBar(stringResource(R.string.feature_player), onBack) {
                IconButton(onClick = { onOpenReader(playback.currentVerse) }, modifier = Modifier.testTag("player-open-reader")) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = stringResource(R.string.player_open_reader))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val c = content
        if (c == null) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp).testTag("player-screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReciterChip(playback, onClick = { sheet = PlayerSheet.RECITER })
            NowPlayingVerse(
                playback = playback,
                arabic = c.verses.getOrNull(playback.currentVerse - 1)?.let {
                    if (c.preferences.arabicScript == ArabicScript.HAFS_KFGQPC) it.textHafs else it.textTanzil
                }.orEmpty(),
                translation = c.translation[playback.currentVerse],
                script = c.preferences.arabicScript,
                modifier = Modifier.weight(1f),
            )
            playback.error?.let { ErrorBanner(it, onRetry = viewModel::retry) }
            RepeatStatusLine(playback)
            PlaybackProgress(viewModel.position, playback.durationMs, playback.isBuffering, onSeek = viewModel::seekTo)
            TransportControls(
                playback = playback,
                onToggle = viewModel::togglePlay,
                onPrevious = viewModel::previous,
                onNext = viewModel::next,
                onSeekBack = { viewModel.seekBy(-10_000) },
                onSeekForward = { viewModel.seekBy(10_000) },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                AssistChip(
                    onClick = { sheet = PlayerSheet.SPEED },
                    label = { Text(formatSpeed(playback.speed)) },
                    leadingIcon = { Icon(Icons.Filled.Speed, null, Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("player-speed-button"),
                )
                AssistChip(
                    onClick = { sheet = PlayerSheet.REPEAT },
                    label = { Text(repeatSummary(playback.repeat)) },
                    leadingIcon = { Icon(Icons.Filled.Repeat, null, Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("player-repeat-button"),
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        when (sheet) {
            PlayerSheet.SPEED -> SpeedSheet(
                speed = playback.speed,
                step = c.preferences.speedStep,
                onSpeed = viewModel::setSpeed,
                onStep = viewModel::stepSpeed,
                onStepSize = viewModel::setSpeedStep,
                onDismiss = { sheet = PlayerSheet.NONE },
            )
            PlayerSheet.REPEAT -> RepeatSheet(
                initial = playback.repeat,
                currentVerse = playback.currentVerse,
                onApply = { viewModel.applyRepeat(it); sheet = PlayerSheet.NONE },
                onDismiss = { sheet = PlayerSheet.NONE },
            )
            PlayerSheet.RECITER -> ReciterSheet(
                reciters = c.reciters,
                selectedId = playback.reciterId ?: c.preferences.reciterId,
                downloads = c.downloads,
                storageBytes = c.storageBytes,
                actions = ReciterSheetActions(
                    onSelect = { viewModel.selectReciter(it) },
                    onDownload = viewModel::download,
                    onPause = viewModel::pauseDownload,
                    onCancel = viewModel::cancelDownload,
                    onDelete = viewModel::deleteDownload,
                ),
                onDismiss = { sheet = PlayerSheet.NONE },
            )
            PlayerSheet.NONE -> Unit
        }
    }
}

@Composable
private fun ReciterChip(playback: PlaybackState, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.testTag("player-reciter-chip"),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (playback.isOfflineSource) Icons.Filled.CloudDone else Icons.Filled.Headphones,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.size(8.dp))
            Column {
                Text(stringResource(R.string.player_reciter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(playback.reciterName.ifEmpty { "…" }, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.size(12.dp))
            Text(
                stringResource(if (playback.isOfflineSource) R.string.player_offline else R.string.player_streaming),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun NowPlayingVerse(
    playback: PlaybackState,
    arabic: String,
    translation: String?,
    script: ArabicScript,
    modifier: Modifier = Modifier,
) {
    val extra = WaqiahThemeExtras.colors
    Surface(
        modifier = modifier.fillMaxWidth().testTag("player-now-playing"),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        AnimatedContent(
            targetState = playback.currentVerse,
            transitionSpec = { (fadeIn() + slideInVertically { it / 8 }) togetherWith fadeOut() },
            label = "now-playing",
        ) { verse ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                VerseNumberBadge(verse, size = 48.dp)
                Text(
                    text = arabic,
                    fontFamily = script.fontFamily(),
                    fontSize = 30.sp,
                    lineHeight = 58.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("player-verse-arabic"),
                )
                translation?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("player-verse-translation"),
                    )
                }
                Text(
                    stringResource(R.string.player_verse_of, verse.toPersianDigits(), RepeatSettings.LAST_VERSE.toPersianDigits()),
                    style = MaterialTheme.typography.labelMedium,
                    color = extra.gold,
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(error: PlaybackError, onRetry: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().testTag("player-error-banner")) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (error == PlaybackError.NETWORK) Icon(Icons.Outlined.WifiOff, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Text(
                stringResource(
                    when (error) {
                        PlaybackError.NETWORK -> R.string.player_error_network
                        PlaybackError.SOURCE -> R.string.player_error_source
                        PlaybackError.UNKNOWN -> R.string.player_error_unknown
                    }
                ),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            TextButton(onClick = onRetry, modifier = Modifier.testTag("player-error-retry")) { Text(stringResource(R.string.state_retry)) }
        }
    }
}

@Composable
private fun RepeatStatusLine(playback: PlaybackState) {
    val s = playback.repeat
    val text = when {
        playback.isInRepeatPause -> stringResource(R.string.player_repeat_pause)
        playback.isFinished -> stringResource(R.string.player_finished)
        s.verseRepeatCount == RepeatSettings.UNLIMITED ->
            stringResource(R.string.repeat_status_unlimited, (playback.versePlays + 1).toPersianDigits())
        s.verseRepeatCount > 1 ->
            stringResource(R.string.repeat_status, (playback.versePlays + 1).toPersianDigits(), s.verseRepeatCount.toPersianDigits())
        else -> ""
    }
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().height(20.dp).testTag("player-repeat-status"),
    )
}

/** Isolated so position ticks only recompose this composable. */
@Composable
private fun PlaybackProgress(position: StateFlow<Long>, durationMs: Long, buffering: Boolean, onSeek: (Long) -> Unit) {
    val pos by position.collectAsStateWithLifecycle()
    var dragging by remember { mutableStateOf<Float?>(null) }
    val duration = durationMs.coerceAtLeast(1L)
    Column(Modifier.fillMaxWidth()) {
        if (buffering) {
            LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp).testTag("player-buffering"))
        } else {
            Spacer(Modifier.height(2.dp))
        }
        Slider(
            value = dragging ?: (pos.toFloat() / duration).coerceIn(0f, 1f),
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let { onSeek((it * duration).toLong()) }
                dragging = null
            },
            modifier = Modifier.testTag("player-progress-slider"),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(formatDuration(dragging?.let { (it * duration).toLong() } ?: pos), style = MaterialTheme.typography.labelMedium, modifier = Modifier.testTag("player-position"))
            Spacer(Modifier.weight(1f))
            Text(formatDuration(durationMs), style = MaterialTheme.typography.labelMedium, modifier = Modifier.testTag("player-duration"))
        }
    }
}

@Composable
private fun TransportControls(
    playback: PlaybackState,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
) {
    // Audio controls keep media (LTR) order: back on the left, forward on the right.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) { Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onSeekBack, modifier = Modifier.testTag("player-seek-back")) {
            Icon(Icons.Filled.Replay10, stringResource(R.string.player_seek_back))
        }
        IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp).testTag("player-previous-verse")) {
            Icon(Icons.Filled.SkipPrevious, stringResource(R.string.player_prev_verse), Modifier.size(32.dp))
        }
        Box(contentAlignment = Alignment.Center) {
            FilledIconButton(
                onClick = onToggle,
                modifier = Modifier.size(76.dp).testTag("player-play-pause"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                val playing = playback.isPlaying || playback.isInRepeatPause
                Icon(
                    if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (playing) R.string.player_pause else R.string.player_play),
                    modifier = Modifier.size(40.dp),
                )
            }
            if (playback.isBuffering) {
                CircularProgressIndicator(Modifier.size(84.dp), strokeWidth = 2.dp, color = WaqiahThemeExtras.colors.gold)
            }
        }
        IconButton(onClick = onNext, modifier = Modifier.size(56.dp).testTag("player-next-verse")) {
            Icon(Icons.Filled.SkipNext, stringResource(R.string.player_next_verse), Modifier.size(32.dp))
        }
        IconButton(onClick = onSeekForward, modifier = Modifier.testTag("player-seek-forward")) {
            Icon(Icons.Filled.Forward10, stringResource(R.string.player_seek_forward))
        }
    } }
}

@Composable
fun repeatSummary(s: RepeatSettings): String = when (s.scope) {
    RepeatScope.VERSE -> stringResource(
        R.string.repeat_summary_verse,
        if (s.verseRepeatCount == RepeatSettings.UNLIMITED) "∞" else s.verseRepeatCount.toPersianDigits(),
    )
    RepeatScope.RANGE -> stringResource(R.string.repeat_summary_range, s.rangeStart.toPersianDigits(), s.rangeEnd.toPersianDigits())
    RepeatScope.SURAH -> if (s.verseRepeatCount == 1 && s.rangeRepeatCount == 1) {
        stringResource(R.string.repeat_summary_off)
    } else {
        stringResource(R.string.repeat_summary_surah)
    }
}

@Composable
internal fun SheetTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
internal fun SheetLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}

@Composable
internal fun ColumnScope.SheetBottomSpacer() = Spacer(Modifier.height(24.dp))

@Composable
internal fun Dot() = Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
