package com.alwaqiah.quran.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.model.ThemeMode
import com.alwaqiah.quran.domain.model.UserPreferences
import com.alwaqiah.quran.presentation.components.LoadingState
import com.alwaqiah.quran.presentation.components.WaqiahTopBar
import com.alwaqiah.quran.presentation.theme.fontFamily
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { WaqiahTopBar(stringResource(R.string.feature_settings), onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        val p = state.preferences
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("settings-screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection(stringResource(R.string.settings_section_appearance)) {
                Label(stringResource(R.string.settings_theme))
                val modes = ThemeMode.entries
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("settings-theme-selector")) {
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = p.themeMode == mode,
                            onClick = { viewModel.setTheme(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                            modifier = Modifier.testTag("settings-theme-${mode.name.lowercase()}"),
                        ) {
                            Text(
                                stringResource(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> R.string.theme_system
                                        ThemeMode.LIGHT -> R.string.theme_light
                                        ThemeMode.DARK -> R.string.theme_dark
                                    }
                                )
                            )
                        }
                    }
                }

                Label(stringResource(R.string.settings_script))
                Column(Modifier.selectableGroup()) {
                    RadioRow(stringResource(R.string.script_hafs), p.arabicScript == ArabicScript.HAFS_KFGQPC, "settings-script-hafs") {
                        viewModel.setScript(ArabicScript.HAFS_KFGQPC)
                    }
                    RadioRow(stringResource(R.string.script_tanzil), p.arabicScript == ArabicScript.TANZIL_AMIRI, "settings-script-tanzil") {
                        viewModel.setScript(ArabicScript.TANZIL_AMIRI)
                    }
                }

                FontSizeSlider(p, state.previewVerse?.let {
                    if (p.arabicScript == ArabicScript.HAFS_KFGQPC) it.textHafs else it.textTanzil
                }, onCommit = viewModel::setFontSize)
                LineSpacingSlider(p.arabicLineSpacing, viewModel::setLineSpacing)
            }

            SettingsSection(stringResource(R.string.settings_section_translation)) {
                Label(stringResource(R.string.settings_primary_translation))
                Column(Modifier.selectableGroup()) {
                    state.persianEditions.forEach { edition ->
                        RadioRow(edition.name, p.primaryTranslationId == edition.id, "settings-translation-${edition.id}") {
                            viewModel.setTranslation(edition.id)
                        }
                    }
                }
                SwitchRow(stringResource(R.string.settings_show_english), p.showEnglishTranslation, "settings-show-english", viewModel::setShowEnglish)
            }

            SettingsSection(stringResource(R.string.settings_section_audio)) {
                Label(stringResource(R.string.settings_reciter))
                Column(Modifier.selectableGroup()) {
                    state.reciters.forEach { reciter ->
                        RadioRow(reciter.nameFa, p.reciterId == reciter.id, "settings-reciter-${reciter.id}") {
                            viewModel.setReciter(reciter.id)
                        }
                    }
                }
                SpeedControls(p, onSpeed = viewModel::setSpeed, onStep = viewModel::setSpeedStep)
                RepeatCountRow(p.repeatCount, viewModel::setRepeatCount)
                PauseSlider(p.pauseBetweenRepeatsMs, viewModel::setPause)
                SwitchRow(stringResource(R.string.settings_download_wifi), p.downloadOnWifiOnly, "settings-wifi-only", viewModel::setWifiOnly)
            }

            SettingsSection(stringResource(R.string.settings_section_reading)) {
                SwitchRow(stringResource(R.string.settings_auto_scroll), p.autoScroll, "settings-auto-scroll", viewModel::setAutoScroll)
                SwitchRow(stringResource(R.string.settings_haptics), p.hapticFeedback, "settings-haptics", viewModel::setHaptics)
                SwitchRow(stringResource(R.string.settings_notifications), p.notificationsEnabled, "settings-notifications", viewModel::setNotifications)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun RadioRow(label: String, selected: Boolean, tag: String, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** Slider keeps a local value while dragging and persists only when released. */
@Composable
private fun FontSizeSlider(p: UserPreferences, previewText: String?, onCommit: (Float) -> Unit) {
    var value by remember(p.arabicFontSizeSp) { mutableFloatStateOf(p.arabicFontSizeSp) }
    Label(stringResource(R.string.settings_font_size) + " — " + value.roundToInt().toPersianDigits())
    Slider(
        value = value,
        onValueChange = { value = it },
        onValueChangeFinished = { onCommit(value) },
        valueRange = UserPreferences.MIN_FONT_SIZE..UserPreferences.MAX_FONT_SIZE,
        steps = (UserPreferences.MAX_FONT_SIZE - UserPreferences.MIN_FONT_SIZE).toInt() / 2 - 1,
        modifier = Modifier.testTag("settings-font-size-slider"),
    )
    previewText?.let {
        Text(
            it,
            fontFamily = p.arabicScript.fontFamily(),
            fontSize = value.sp,
            lineHeight = (value * 1.9f).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().testTag("settings-font-preview"),
        )
    }
}

@Composable
private fun LineSpacingSlider(spacing: Float, onCommit: (Float) -> Unit) {
    var value by remember(spacing) { mutableFloatStateOf(spacing) }
    Label(stringResource(R.string.settings_line_spacing) + " — " + String.format(Locale.US, "%.1f", value).toPersianDigits())
    Slider(
        value = value,
        onValueChange = { value = it },
        onValueChangeFinished = { onCommit(value) },
        valueRange = 1.4f..2.6f,
        steps = 11,
        modifier = Modifier.testTag("settings-line-spacing-slider"),
    )
}

private fun Float.formatSpeed(): String = String.format(Locale.US, "%.2f×", this).toPersianDigits()

@Composable
private fun SpeedControls(p: UserPreferences, onSpeed: (Float) -> Unit, onStep: (Float) -> Unit) {
    var speed by remember(p.playbackSpeed) { mutableFloatStateOf(p.playbackSpeed) }
    Label(stringResource(R.string.settings_speed) + " — " + speed.formatSpeed())
    val range = UserPreferences.MAX_SPEED - UserPreferences.MIN_SPEED
    Slider(
        value = speed,
        onValueChange = { speed = it },
        onValueChangeFinished = { onSpeed(speed) },
        valueRange = UserPreferences.MIN_SPEED..UserPreferences.MAX_SPEED,
        steps = (range / p.speedStep).roundToInt() - 1,
        modifier = Modifier.testTag("settings-speed-slider"),
    )
    Label(stringResource(R.string.settings_speed_step))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UserPreferences.SPEED_STEPS.forEach { step ->
            FilterChip(
                selected = p.speedStep == step,
                onClick = { onStep(step) },
                label = { Text(String.format(Locale.US, "%.2f", step).toPersianDigits()) },
                modifier = Modifier.testTag("settings-speed-step-$step"),
            )
        }
    }
}

@Composable
private fun RepeatCountRow(count: Int, onChange: (Int) -> Unit) {
    Label(stringResource(R.string.settings_repeat_count))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1, 3, 5, 10, 0).forEach { option ->
            FilterChip(
                selected = count == option,
                onClick = { onChange(option) },
                label = { Text(if (option == 0) stringResource(R.string.repeat_unlimited) else option.toPersianDigits()) },
                modifier = Modifier.testTag("settings-repeat-$option"),
            )
        }
    }
}

@Composable
private fun PauseSlider(pauseMs: Long, onCommit: (Long) -> Unit) {
    var seconds by remember(pauseMs) { mutableFloatStateOf(pauseMs / 1000f) }
    Label(stringResource(R.string.settings_pause_between_repeats) + " — " + stringResource(R.string.settings_seconds, seconds.roundToInt().toPersianDigits()))
    Slider(
        value = seconds,
        onValueChange = { seconds = it },
        onValueChangeFinished = { onCommit(seconds.roundToInt() * 1000L) },
        valueRange = 0f..(UserPreferences.MAX_PAUSE_MS / 1000f),
        steps = (UserPreferences.MAX_PAUSE_MS / 1000).toInt() - 1,
        modifier = Modifier.testTag("settings-pause-slider"),
    )
}
