package com.alwaqiah.quran

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.alwaqiah.quran.domain.model.ThemeMode
import com.alwaqiah.quran.domain.repository.UserPreferencesRepository
import com.alwaqiah.quran.presentation.navigation.WaqiahNavHost
import com.alwaqiah.quran.presentation.theme.WaqiahTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    /** null until DataStore has been read, so the splash avoids a theme flash. */
    val themeMode: StateFlow<ThemeMode?> = preferencesRepository.preferences
        .map<_, ThemeMode?> { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.themeMode.value == null }
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            WaqiahTheme(themeMode = themeMode ?: ThemeMode.SYSTEM) {
                // The interface is Persian-only, so the whole UI is laid out right-to-left.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // Exposes Compose testTags as resource ids for UI Automator / Appium.
                    Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                        WaqiahNavHost()
                    }
                }
            }
        }
    }
}
