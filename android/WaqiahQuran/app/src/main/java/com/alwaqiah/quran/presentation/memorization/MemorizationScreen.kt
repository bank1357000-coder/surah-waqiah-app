package com.alwaqiah.quran.presentation.memorization

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.memorization.MemorizationSession
import com.alwaqiah.quran.domain.memorization.MemorizationStatus
import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.playback.PlaybackState
import com.alwaqiah.quran.presentation.components.LoadingState
import com.alwaqiah.quran.presentation.components.VerseNumberBadge
import com.alwaqiah.quran.presentation.components.WaqiahTopBar
import com.alwaqiah.quran.presentation.theme.fontFamily

@Composable
fun MemorizationScreen(onBack: () -> Unit, viewModel: MemorizationViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val active by viewModel.sessionActive.collectAsStateWithLifecycle()
    val revealed by viewModel.revealed.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { WaqiahTopBar(stringResource(R.string.feature_memorization), onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).testTag("memorization-screen"),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, padding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "progress") { ProgressCard(state) }
            if (active) {
                item(key = "focus") {
                    val verse = playback.currentVerse
                    FocusCard(
                        state = state,
                        playback = playback,
                        isRevealed = verse in revealed,
                        onReveal = { viewModel.reveal(verse) },
                        onToggle = viewModel::togglePlay,
                        onNext = viewModel::next,
                        onPrevious = viewModel::previous,
                        onMark = { viewModel.mark(verse, it) },
                        onStop = viewModel::stop,
                    )
                }
            } else {
                item(key = "setup") { SessionSetup(state.session, viewModel::updateSession, onStart = { viewModel.start() }) }
            }
            val review = state.progress.values
                .filter { it.status == MemorizationStatus.NEEDS_REVISION || it.status == MemorizationStatus.MEMORIZED }
                .sortedWith(compareBy({ it.status != MemorizationStatus.NEEDS_REVISION }, { it.verseNumber }))
            if (review.isNotEmpty()) {
                item(key = "review-title") { Text(stringResource(R.string.memo_review_title), style = MaterialTheme.typography.titleMedium) }
                items(review, key = { "review-${it.verseNumber}" }) { p ->
                    Surface(
                        onClick = { viewModel.practiceVerse(p.verseNumber) },
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().testTag("memo-review-${p.verseNumber}"),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            VerseNumberBadge(p.verseNumber)
                            Text(
                                stringResource(if (p.status == MemorizationStatus.NEEDS_REVISION) R.string.memo_status_revision else R.string.memo_status_memorized),
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                color = if (p.status == MemorizationStatus.NEEDS_REVISION) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                            Text(stringResource(R.string.memo_repetitions, p.repetitions.toPersianDigits()), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(state: MemorizationUiState) {
    val goal = state.session.dailyGoalVerses
    val goalFraction = com.alwaqiah.quran.domain.memorization.MemorizationStats.goalFraction(state.today, goal)
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().testTag("memo-progress-card")) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.memo_memorized_of, state.summary.memorized.toPersianDigits(), state.summary.total.toPersianDigits()),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            LinearProgressIndicator(progress = { state.summary.fraction }, modifier = Modifier.fillMaxWidth().testTag("memo-total-progress"))
            Text(
                stringResource(R.string.memo_today, state.today.versesPracticed.toPersianDigits(), goal.toPersianDigits(), state.today.repetitions.toPersianDigits()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            LinearProgressIndicator(progress = { goalFraction }, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxWidth().testTag("memo-goal-progress"))
            Text(
                stringResource(R.string.memo_counts, state.summary.learning.toPersianDigits(), state.summary.needsRevision.toPersianDigits()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SessionSetup(session: MemorizationSession, onChange: ((MemorizationSession) -> MemorizationSession) -> Unit, onStart: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().testTag("memo-setup")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StepRow(stringResource(R.string.repeat_range_from), session.rangeStart, 1, 96, "memo-start") { v -> onChange { it.copy(rangeStart = v, rangeEnd = maxOf(v, it.rangeEnd)) } }
            StepRow(stringResource(R.string.repeat_range_to), session.rangeEnd, session.rangeStart, 96, "memo-end") { v -> onChange { it.copy(rangeEnd = v) } }
            StepRow(stringResource(R.string.repeat_verse_count), session.verseRepeatCount, 1, 999, "memo-repeat") { v -> onChange { it.copy(verseRepeatCount = v) } }
            StepRow(stringResource(R.string.memo_pause_seconds), (session.pauseBetweenRepeatsMs / 1000).toInt(), 0, 10, "memo-pause") { v -> onChange { it.copy(pauseBetweenRepeatsMs = v * 1000L) } }
            StepRow(stringResource(R.string.memo_daily_goal), session.dailyGoalVerses, 1, 96, "memo-goal") { v -> onChange { it.copy(dailyGoalVerses = v) } }
            SwitchLine(stringResource(R.string.memo_hide_arabic), session.hideArabic, "memo-hide-arabic") { v -> onChange { it.copy(hideArabic = v) } }
            SwitchLine(stringResource(R.string.memo_hide_translation), session.hideTranslation, "memo-hide-translation") { v -> onChange { it.copy(hideTranslation = v) } }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 48.dp).testTag("memo-start-button")) {
                Text(
                    if (session.lastVerse in (session.rangeStart + 1)..session.rangeEnd) {
                        stringResource(R.string.memo_resume, session.lastVerse.toPersianDigits())
                    } else {
                        stringResource(R.string.memo_start)
                    }
                )
            }
        }
    }
}

@Composable
private fun FocusCard(
    state: MemorizationUiState,
    playback: PlaybackState,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onMark: (MemorizationStatus) -> Unit,
    onStop: () -> Unit,
) {
    val verse = playback.currentVerse
    val v = state.verses.getOrNull(verse - 1)
    val hideArabic = state.session.hideArabic && !isRevealed
    val hideTranslation = state.session.hideTranslation && !isRevealed
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().testTag("memo-focus-card")) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            VerseNumberBadge(verse, size = 48.dp)
            Text(
                stringResource(R.string.repeat_status, (playback.versePlays + 1).toPersianDigits(), state.session.verseRepeatCount.toPersianDigits()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.testTag("memo-repeat-status"),
            )
            AnimatedContent(targetState = hideArabic, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "reveal") { hidden ->
                if (hidden) {
                    OutlinedButton(onClick = onReveal, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).testTag("memo-reveal-button")) {
                        Icon(Icons.Outlined.Visibility, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.memo_tap_to_reveal))
                    }
                } else {
                    Text(
                        text = v?.let { if (state.preferences.arabicScript == ArabicScript.HAFS_KFGQPC) it.textHafs else it.textTanzil }.orEmpty(),
                        fontFamily = state.preferences.arabicScript.fontFamily(),
                        fontSize = state.preferences.arabicFontSizeSp.sp,
                        lineHeight = (state.preferences.arabicFontSizeSp * state.preferences.arabicLineSpacing).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("memo-verse-arabic"),
                    )
                }
            }
            if (!hideTranslation) {
                state.translation[verse]?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.testTag("memo-verse-translation"))
                }
            }
            if (playback.isInRepeatPause) Text(stringResource(R.string.player_repeat_pause), style = MaterialTheme.typography.labelMedium)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = onPrevious, modifier = Modifier.testTag("memo-previous")) { Icon(Icons.Filled.SkipPrevious, stringResource(R.string.player_prev_verse)) }
                    FilledIconButton(onClick = onToggle, modifier = Modifier.size(64.dp).testTag("memo-play-pause")) {
                        val playing = playback.isPlaying || playback.isInRepeatPause
                        Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, stringResource(if (playing) R.string.player_pause else R.string.player_play), Modifier.size(32.dp))
                    }
                    IconButton(onClick = onNext, modifier = Modifier.testTag("memo-next")) { Icon(Icons.Filled.SkipNext, stringResource(R.string.player_next_verse)) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onMark(MemorizationStatus.MEMORIZED) }, modifier = Modifier.testTag("memo-mark-memorized")) { Text(stringResource(R.string.memo_mark_memorized)) }
                OutlinedButton(onClick = { onMark(MemorizationStatus.NEEDS_REVISION) }, modifier = Modifier.testTag("memo-mark-revision")) { Text(stringResource(R.string.memo_mark_revision)) }
            }
            TextButton(onClick = onStop, modifier = Modifier.testTag("memo-stop-button")) { Text(stringResource(R.string.memo_stop)) }
        }
    }
}

@Composable
private fun StepRow(label: String, value: Int, min: Int, max: Int, tag: String, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = { onChange((value - 1).coerceAtLeast(min)) }, enabled = value > min, modifier = Modifier.testTag("$tag-minus")) { Icon(Icons.Filled.Remove, null) }
        Text(value.toPersianDigits(), modifier = Modifier.width(40.dp).testTag("$tag-value"), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { onChange((value + 1).coerceAtMost(max)) }, enabled = value < max, modifier = Modifier.testTag("$tag-plus")) { Icon(Icons.Filled.Add, null) }
    }
}

@Composable
private fun SwitchLine(label: String, checked: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(checked, role = Role.Switch, onValueChange = onChange).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = null)
    }
}
