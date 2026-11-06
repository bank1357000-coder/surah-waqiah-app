package com.alwaqiah.quran.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.model.Reciter
import com.alwaqiah.quran.domain.model.ThemeMode
import com.alwaqiah.quran.domain.model.TranslationEdition
import com.alwaqiah.quran.domain.model.UserPreferences
import com.alwaqiah.quran.domain.model.Verse
import com.alwaqiah.quran.domain.repository.QuranRepository
import com.alwaqiah.quran.domain.repository.ReciterRepository
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val preferences: UserPreferences = UserPreferences(),
    val persianEditions: List<TranslationEdition> = emptyList(),
    val reciters: List<Reciter> = emptyList(),
    /** Real verse 1 used to preview the chosen font and size. */
    val previewVerse: Verse? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.preferences,
        quranRepository.observeTranslationEditions().map { list -> list.filter { it.language == "fa" } },
        reciterRepository.observeReciters(),
        quranRepository.observeVerses().map { it.firstOrNull() },
    ) { prefs, editions, reciters, preview ->
        SettingsUiState(false, prefs, editions, reciters, preview)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private fun save(block: suspend UserPreferencesRepository.() -> Unit) {
        viewModelScope.launch { preferencesRepository.block() }
    }

    fun setTheme(mode: ThemeMode) = save { this.setThemeMode(mode) }
    fun setFontSize(size: Float) = save { this.setArabicFontSize(size) }
    fun setLineSpacing(multiplier: Float) = save { this.setArabicLineSpacing(multiplier) }
    fun setScript(script: ArabicScript) = save { this.setArabicScript(script) }
    fun setTranslation(id: String) = save { this.setPrimaryTranslation(id) }
    fun setShowEnglish(show: Boolean) = save { this.setShowEnglishTranslation(show) }
    fun setReciter(id: Int) = save { this.setReciter(id) }
    fun setSpeed(speed: Float) = save { this.setPlaybackSpeed(speed) }
    fun setSpeedStep(step: Float) = save { this.setSpeedStep(step) }
    fun setRepeatCount(count: Int) = save { this.setRepeatCount(count) }
    fun setPause(ms: Long) = save { this.setPauseBetweenRepeats(ms) }
    fun setAutoScroll(on: Boolean) = save { this.setAutoScroll(on) }
    fun setHaptics(on: Boolean) = save { this.setHapticFeedback(on) }
    fun setNotifications(on: Boolean) = save { this.setNotificationsEnabled(on) }
    fun setWifiOnly(on: Boolean) = save { this.setDownloadOnWifiOnly(on) }
}
