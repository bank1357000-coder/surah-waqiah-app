package com.alwaqiah.quran.presentation.player

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.model.DownloadStatus
import com.alwaqiah.quran.domain.model.Reciter
import com.alwaqiah.quran.domain.playback.PlaybackSpeed
import com.alwaqiah.quran.domain.playback.RepeatScope
import com.alwaqiah.quran.domain.playback.RepeatSettings
import com.alwaqiah.quran.presentation.components.formatBytes
import com.alwaqiah.quran.presentation.components.formatSpeed
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerBottomSheet(onDismiss: () -> Unit, tag: String, content: @Composable () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag(tag),
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp),
        ) { content() }
    }
}

@Composable
fun SpeedSheet(
    speed: Float,
    step: Float,
    onSpeed: (Float) -> Unit,
    onStep: (Int) -> Unit,
    onStepSize: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    PlayerBottomSheet(onDismiss, "speed-sheet") {
        SheetTitle(stringResource(R.string.settings_speed))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            FilledTonalIconButton(onClick = { onStep(-1) }, enabled = speed > PlaybackSpeed.MIN, modifier = Modifier.testTag("speed-decrease")) {
                Icon(Icons.Filled.Remove, null)
            }
            Text(formatSpeed(speed), style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(horizontal = 24.dp).testTag("speed-value"))
            FilledTonalIconButton(onClick = { onStep(1) }, enabled = speed < PlaybackSpeed.MAX, modifier = Modifier.testTag("speed-increase")) {
                Icon(Icons.Filled.Add, null)
            }
        }
        SheetLabel(stringResource(R.string.speed_presets))
        ChipRow {
            PlaybackSpeed.PRESETS.forEach { preset ->
                FilterChip(selected = speed == preset, onClick = { onSpeed(preset) }, label = { Text(formatSpeed(preset)) }, modifier = Modifier.testTag("speed-preset-$preset"))
            }
        }
        SheetLabel(stringResource(R.string.settings_speed_step))
        ChipRow {
            PlaybackSpeed.STEPS.forEach { s ->
                FilterChip(selected = step == s, onClick = { onStepSize(s) }, label = { Text(formatSpeed(s).removeSuffix("×")) }, modifier = Modifier.testTag("speed-step-$s"))
            }
        }
    }
}

@Composable
fun RepeatSheet(
    initial: RepeatSettings,
    currentVerse: Int,
    onApply: (RepeatSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(initial) {
        mutableStateOf(
            if (initial.scope == RepeatScope.RANGE) initial
            else initial.copy(rangeStart = currentVerse, rangeEnd = minOf(currentVerse + 4, RepeatSettings.LAST_VERSE))
        )
    }
    PlayerBottomSheet(onDismiss, "repeat-sheet") {
        SheetTitle(stringResource(R.string.repeat_title))
        val scopes = RepeatScope.entries
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            scopes.forEachIndexed { i, scope ->
                SegmentedButton(
                    selected = draft.scope == scope,
                    onClick = { draft = draft.copy(scope = scope) },
                    shape = SegmentedButtonDefaults.itemShape(i, scopes.size),
                    modifier = Modifier.testTag("repeat-scope-${scope.name.lowercase()}"),
                ) {
                    Text(stringResource(when (scope) {
                        RepeatScope.VERSE -> R.string.repeat_scope_verse
                        RepeatScope.RANGE -> R.string.repeat_scope_range
                        RepeatScope.SURAH -> R.string.repeat_scope_surah
                    }))
                }
            }
        }

        if (draft.scope == RepeatScope.RANGE) {
            SheetLabel(stringResource(R.string.repeat_range_from))
            Stepper(draft.rangeStart, 1, RepeatSettings.LAST_VERSE, "repeat-range-start") {
                draft = draft.copy(rangeStart = it, rangeEnd = maxOf(it, draft.rangeEnd))
            }
            SheetLabel(stringResource(R.string.repeat_range_to))
            Stepper(draft.rangeEnd, draft.rangeStart, RepeatSettings.LAST_VERSE, "repeat-range-end") {
                draft = draft.copy(rangeEnd = it)
            }
        }

        SheetLabel(stringResource(R.string.repeat_verse_count))
        CountPicker(draft.verseRepeatCount, "repeat-verse-count") { draft = draft.copy(verseRepeatCount = it) }

        if (draft.scope != RepeatScope.VERSE) {
            SheetLabel(stringResource(if (draft.scope == RepeatScope.RANGE) R.string.repeat_range_count else R.string.repeat_surah_count))
            CountPicker(draft.rangeRepeatCount, "repeat-range-count") { draft = draft.copy(rangeRepeatCount = it) }
        }

        val pauseSeconds = (draft.pauseBetweenRepeatsMs / 1000f)
        SheetLabel(stringResource(R.string.settings_pause_between_repeats) + " — " +
            stringResource(R.string.settings_seconds, pauseSeconds.roundToInt().toPersianDigits()))
        Slider(
            value = pauseSeconds,
            onValueChange = { draft = draft.copy(pauseBetweenRepeatsMs = it.roundToInt() * 1000L) },
            valueRange = 0f..(RepeatSettings.MAX_PAUSE_MS / 1000f),
            steps = (RepeatSettings.MAX_PAUSE_MS / 1000).toInt() - 1,
            modifier = Modifier.testTag("repeat-pause-slider"),
        )

        if (draft.scope != RepeatScope.SURAH) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .toggleable(draft.continueToNextRange, role = Role.Switch) { draft = draft.copy(continueToNextRange = it) }
                    .testTag("repeat-continue-switch"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(if (draft.scope == RepeatScope.VERSE) R.string.repeat_continue_verse else R.string.repeat_continue_range),
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = draft.continueToNextRange, onCheckedChange = null)
            }
        }

        Button(onClick = { onApply(draft.normalized()) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 48.dp).testTag("repeat-apply-button")) {
            Text(stringResource(R.string.repeat_apply))
        }
    }
}

/** 1, 2, 3, 5, 10, ∞ plus a custom stepper. */
@Composable
private fun CountPicker(value: Int, tag: String, onChange: (Int) -> Unit) {
    val presets = listOf(1, 2, 3, 5, 10, RepeatSettings.UNLIMITED)
    ChipRow {
        presets.forEach { option ->
            FilterChip(
                selected = value == option,
                onClick = { onChange(option) },
                label = { Text(if (option == RepeatSettings.UNLIMITED) stringResource(R.string.repeat_unlimited) else option.toPersianDigits()) },
                modifier = Modifier.testTag("$tag-$option"),
            )
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.repeat_custom), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 8.dp))
        Stepper(if (value == RepeatSettings.UNLIMITED) 1 else value, 1, RepeatSettings.MAX_COUNT, "$tag-custom", onChange)
    }
}

@Composable
private fun Stepper(value: Int, min: Int, max: Int, tag: String, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange((value - 1).coerceAtLeast(min)) }, enabled = value > min, modifier = Modifier.testTag("$tag-minus")) {
            Icon(Icons.Filled.Remove, null)
        }
        Text(value.toPersianDigits(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(48.dp).testTag("$tag-value"))
        IconButton(onClick = { onChange((value + 1).coerceAtMost(max)) }, enabled = value < max, modifier = Modifier.testTag("$tag-plus")) {
            Icon(Icons.Filled.Add, null)
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

data class ReciterSheetActions(
    val onSelect: (Int) -> Unit,
    val onDownload: (Int) -> Unit,
    val onPause: (Int) -> Unit,
    val onCancel: (Int) -> Unit,
    val onDelete: (Int) -> Unit,
)

@Composable
fun ReciterSheet(
    reciters: List<Reciter>,
    selectedId: Int,
    downloads: Map<Int, DownloadStatus>,
    storageBytes: Long,
    actions: ReciterSheetActions,
    onDismiss: () -> Unit,
) {
    PlayerBottomSheet(onDismiss, "reciter-sheet") {
        SheetTitle(stringResource(R.string.reciter_title))
        reciters.forEach { reciter ->
            val status = downloads[reciter.id] ?: DownloadStatus.NotDownloaded
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .selectable(selected = reciter.id == selectedId, role = Role.RadioButton) { actions.onSelect(reciter.id) }
                        .testTag("reciter-row-${reciter.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = reciter.id == selectedId, onClick = null)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(reciter.nameFa, style = MaterialTheme.typography.titleSmall)
                        Text(downloadLabel(status), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.testTag("reciter-download-status-${reciter.id}"))
                    }
                    DownloadButtons(reciter.id, status, actions)
                }
                if (status is DownloadStatus.Downloading) {
                    val fraction = status.fraction
                    if (fraction != null) LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                    else LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
        }
        Text(
            stringResource(R.string.storage_usage, formatBytes(storageBytes)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp).testTag("storage-usage"),
        )
    }
}

@Composable
private fun DownloadButtons(id: Int, status: DownloadStatus, actions: ReciterSheetActions) {
    @Composable
    fun action(icon: androidx.compose.ui.graphics.vector.ImageVector, label: Int, tag: String, onClick: () -> Unit) {
        IconButton(onClick = onClick, modifier = Modifier.testTag("$tag-$id")) {
            Icon(icon, contentDescription = stringResource(label), modifier = Modifier.size(22.dp))
        }
    }
    when (status) {
        DownloadStatus.NotDownloaded, DownloadStatus.Failed ->
            action(Icons.Filled.Download, R.string.download_action, "reciter-download") { actions.onDownload(id) }
        DownloadStatus.Queued, is DownloadStatus.Downloading -> Row {
            action(Icons.Filled.Pause, R.string.download_pause, "reciter-download-pause") { actions.onPause(id) }
            action(Icons.Filled.Close, R.string.download_cancel, "reciter-download-cancel") { actions.onCancel(id) }
        }
        is DownloadStatus.Paused -> Row {
            action(Icons.Filled.PlayArrow, R.string.download_resume, "reciter-download-resume") { actions.onDownload(id) }
            action(Icons.Filled.Close, R.string.download_cancel, "reciter-download-cancel") { actions.onCancel(id) }
        }
        is DownloadStatus.Downloaded ->
            action(Icons.Filled.Delete, R.string.download_delete, "reciter-download-delete") { actions.onDelete(id) }
    }
}

@Composable
private fun downloadLabel(status: DownloadStatus): String = when (status) {
    DownloadStatus.NotDownloaded -> stringResource(R.string.player_streaming)
    DownloadStatus.Queued -> stringResource(R.string.download_queued)
    is DownloadStatus.Downloading -> stringResource(R.string.download_progress, formatBytes(status.downloadedBytes))
    is DownloadStatus.Paused -> stringResource(R.string.download_paused, formatBytes(status.downloadedBytes))
    is DownloadStatus.Downloaded -> stringResource(R.string.download_done, formatBytes(status.sizeBytes))
    DownloadStatus.Failed -> stringResource(R.string.download_failed)
}
