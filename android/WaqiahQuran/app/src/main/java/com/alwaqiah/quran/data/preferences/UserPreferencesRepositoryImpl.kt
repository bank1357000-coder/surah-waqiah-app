package com.alwaqiah.quran.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.domain.model.ThemeMode
import com.alwaqiah.quran.domain.model.UserPreferences
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val FONT_SIZE = floatPreferencesKey("arabic_font_size")
        val SCRIPT = stringPreferencesKey("arabic_script")
        val PRIMARY_TRANSLATION = stringPreferencesKey("primary_translation")
        val SHOW_ENGLISH = booleanPreferencesKey("show_english")
        val RECITER = intPreferencesKey("reciter_id")
        val SPEED = floatPreferencesKey("playback_speed")
        val SPEED_STEP = floatPreferencesKey("speed_step")
        val REPEAT_COUNT = intPreferencesKey("repeat_count")
        val PAUSE_MS = longPreferencesKey("pause_between_repeats_ms")
        val AUTO_SCROLL = booleanPreferencesKey("auto_scroll")
        val HAPTICS = booleanPreferencesKey("haptic_feedback")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val WIFI_ONLY = booleanPreferencesKey("download_wifi_only")
    }

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toUserPreferences() }
        .distinctUntilChanged()

    private fun Preferences.toUserPreferences(): UserPreferences {
        val d = UserPreferences()
        return UserPreferences(
            themeMode = enumOrDefault(this[Keys.THEME], d.themeMode),
            arabicFontSizeSp = this[Keys.FONT_SIZE] ?: d.arabicFontSizeSp,
            arabicScript = enumOrDefault(this[Keys.SCRIPT], d.arabicScript),
            primaryTranslationId = this[Keys.PRIMARY_TRANSLATION] ?: d.primaryTranslationId,
            showEnglishTranslation = this[Keys.SHOW_ENGLISH] ?: d.showEnglishTranslation,
            englishTranslationId = d.englishTranslationId,
            reciterId = this[Keys.RECITER] ?: d.reciterId,
            playbackSpeed = this[Keys.SPEED] ?: d.playbackSpeed,
            speedStep = this[Keys.SPEED_STEP] ?: d.speedStep,
            repeatCount = this[Keys.REPEAT_COUNT] ?: d.repeatCount,
            pauseBetweenRepeatsMs = this[Keys.PAUSE_MS] ?: d.pauseBetweenRepeatsMs,
            autoScroll = this[Keys.AUTO_SCROLL] ?: d.autoScroll,
            hapticFeedback = this[Keys.HAPTICS] ?: d.hapticFeedback,
            notificationsEnabled = this[Keys.NOTIFICATIONS] ?: d.notificationsEnabled,
            downloadOnWifiOnly = this[Keys.WIFI_ONLY] ?: d.downloadOnWifiOnly,
        )
    }

    private inline fun <reified E : Enum<E>> enumOrDefault(value: String?, default: E): E =
        value?.let { name -> enumValues<E>().firstOrNull { it.name == name } } ?: default

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        dataStore.edit { block(it) }
    }

    override suspend fun setThemeMode(mode: ThemeMode) = update { it[Keys.THEME] = mode.name }

    override suspend fun setArabicFontSize(sizeSp: Float) = update {
        it[Keys.FONT_SIZE] = sizeSp.coerceIn(UserPreferences.MIN_FONT_SIZE, UserPreferences.MAX_FONT_SIZE)
    }

    override suspend fun setArabicScript(script: ArabicScript) = update { it[Keys.SCRIPT] = script.name }

    override suspend fun setPrimaryTranslation(editionId: String) = update { it[Keys.PRIMARY_TRANSLATION] = editionId }

    override suspend fun setShowEnglishTranslation(show: Boolean) = update { it[Keys.SHOW_ENGLISH] = show }

    override suspend fun setReciter(reciterId: Int) = update { it[Keys.RECITER] = reciterId }

    override suspend fun setPlaybackSpeed(speed: Float) = update {
        it[Keys.SPEED] = speed.coerceIn(UserPreferences.MIN_SPEED, UserPreferences.MAX_SPEED)
    }

    override suspend fun setSpeedStep(step: Float) = update { it[Keys.SPEED_STEP] = step }

    override suspend fun setRepeatCount(count: Int) = update { it[Keys.REPEAT_COUNT] = count.coerceAtLeast(0) }

    override suspend fun setPauseBetweenRepeats(ms: Long) = update {
        it[Keys.PAUSE_MS] = ms.coerceIn(0L, UserPreferences.MAX_PAUSE_MS)
    }

    override suspend fun setAutoScroll(enabled: Boolean) = update { it[Keys.AUTO_SCROLL] = enabled }

    override suspend fun setHapticFeedback(enabled: Boolean) = update { it[Keys.HAPTICS] = enabled }

    override suspend fun setNotificationsEnabled(enabled: Boolean) = update { it[Keys.NOTIFICATIONS] = enabled }

    override suspend fun setDownloadOnWifiOnly(enabled: Boolean) = update { it[Keys.WIFI_ONLY] = enabled }
}
