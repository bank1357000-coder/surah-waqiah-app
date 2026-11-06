package com.alwaqiah.quran.presentation.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.shareText
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.model.ArabicScript
import com.alwaqiah.quran.presentation.components.ErrorState
import com.alwaqiah.quran.presentation.components.LoadingState
import com.alwaqiah.quran.presentation.components.WaqiahTopBar
import com.alwaqiah.quran.presentation.theme.WaqiahThemeExtras
import com.alwaqiah.quran.presentation.theme.fontFamily
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun ReaderScreen(onBack: () -> Unit, viewModel: ReaderViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selected by viewModel.selectedVerse.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var initialScrollDone by rememberSaveable { mutableStateOf(false) }

    val copiedMessage = stringResource(R.string.toast_copied)
    val shareTitle = stringResource(R.string.share_verse_title)
    val title = stringResource(R.string.app_name)

    // Record reading history once the user settles on a verse.
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { it.coerceAtLeast(1) } // index 0 is the Bismillah header
            .distinctUntilChanged()
            .debounce(1_200)
            .filter { state is ReaderUiState.Success }
            .collect { viewModel.onVerseVisible(it) }
    }

    Scaffold(
        topBar = { WaqiahTopBar(title = title, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val s = state) {
            ReaderUiState.Loading -> LoadingState(Modifier.padding(padding))
            is ReaderUiState.Error -> ErrorState(stringResource(R.string.state_error_body), onRetry = viewModel::onRetry, modifier = Modifier.padding(padding))
            is ReaderUiState.Success -> {
                LaunchedEffect(s.verses.size) {
                    if (!initialScrollDone && viewModel.startVerse in 1..s.verses.size) {
                        listState.scrollToItem(viewModel.startVerse)
                    }
                    initialScrollDone = true
                }
                val prefs = s.preferences
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).testTag("reader-verse-list"),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "bismillah") {
                        Bismillah(
                            text = if (prefs.arabicScript == ArabicScript.HAFS_KFGQPC) s.surah.bismillahHafs else s.surah.bismillahTanzil,
                            script = prefs.arabicScript,
                            sizeSp = prefs.arabicFontSizeSp,
                        )
                    }
                    items(items = s.verses, key = { it.verse.number }, contentType = { "verse" }) { item ->
                        val number = item.verse.number
                        val reference = stringResource(R.string.verse_reference, number.toPersianDigits())
                        VerseItem(
                            item = item,
                            selected = selected == number,
                            script = prefs.arabicScript,
                            arabicFontSizeSp = prefs.arabicFontSizeSp,
                            onClick = {
                                if (prefs.hapticFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onVerseTapped(number)
                            },
                            onToggleBookmark = {
                                if (prefs.hapticFeedback) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onToggleBookmark(number)
                            },
                            onCopy = {
                                clipboard.setText(AnnotatedString(VerseShareFormatter.format(item, prefs.arabicScript, reference)))
                                scope.launch { snackbar.showSnackbar(copiedMessage) }
                            },
                            onShare = {
                                context.shareText(VerseShareFormatter.format(item, prefs.arabicScript, reference), shareTitle)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Bismillah(text: String, script: ArabicScript, sizeSp: Float) {
    Text(
        text = text,
        fontFamily = script.fontFamily(),
        fontSize = (sizeSp + 2).sp,
        lineHeight = ((sizeSp + 2) * 1.8f).sp,
        textAlign = TextAlign.Center,
        color = WaqiahThemeExtras.colors.gold,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).testTag("reader-bismillah"),
    )
}
