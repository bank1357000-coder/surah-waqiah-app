package com.alwaqiah.quran.presentation.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.presentation.components.LoadingState
import com.alwaqiah.quran.presentation.components.MessageState
import com.alwaqiah.quran.presentation.components.VerseNumberBadge
import com.alwaqiah.quran.presentation.components.WaqiahTopBar
import com.alwaqiah.quran.presentation.theme.QuranFonts

@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onOpenVerse: (Int) -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { WaqiahTopBar(stringResource(R.string.feature_bookmarks), onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.items.isEmpty() -> MessageState(
                icon = Icons.Filled.BookmarkBorder,
                title = stringResource(R.string.bookmarks_empty_title),
                body = stringResource(R.string.bookmarks_empty_body),
                modifier = Modifier.padding(padding).testTag("bookmarks-empty-state"),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).testTag("bookmarks-list"),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, padding.calculateBottomPadding() + 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.bookmark.id }) { item ->
                    val n = item.bookmark.verseNumber
                    Surface(
                        onClick = { onOpenVerse(n) },
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().testTag("bookmark-item-$n"),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            VerseNumberBadge(n)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(
                                    stringResource(R.string.verse_reference, n.toPersianDigits()),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Text(
                                    item.arabicText,
                                    fontFamily = QuranFonts.Hafs,
                                    fontSize = 20.sp,
                                    lineHeight = 38.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = { viewModel.remove(n) }, modifier = Modifier.testTag("bookmark-delete-$n")) {
                                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.bookmark_delete))
                            }
                        }
                    }
                }
            }
        }
    }
}
