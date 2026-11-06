package com.alwaqiah.quran.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alwaqiah.quran.domain.model.Bookmark
import com.alwaqiah.quran.domain.repository.BookmarkRepository
import com.alwaqiah.quran.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarkItem(val bookmark: Bookmark, val arabicText: String)

data class BookmarksUiState(val isLoading: Boolean = true, val items: List<BookmarkItem> = emptyList())

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    quranRepository: QuranRepository,
) : ViewModel() {

    val uiState: StateFlow<BookmarksUiState> = combine(
        bookmarkRepository.observeBookmarks(),
        quranRepository.observeVerses(),
    ) { bookmarks, verses ->
        val byNumber = verses.associateBy { it.number }
        BookmarksUiState(
            isLoading = false,
            items = bookmarks.map { BookmarkItem(it, byNumber[it.verseNumber]?.textHafs.orEmpty()) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookmarksUiState())

    fun remove(verseNumber: Int) {
        viewModelScope.launch { bookmarkRepository.remove(verseNumber) }
    }
}
