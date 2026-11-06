package com.alwaqiah.quran.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alwaqiah.quran.domain.model.ReadingPosition
import com.alwaqiah.quran.domain.model.SurahInfo
import com.alwaqiah.quran.domain.repository.QuranRepository
import com.alwaqiah.quran.domain.repository.ReadingHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val surah: SurahInfo? = null,
    val juz: Int? = null,
    val lastRead: ReadingPosition? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    quranRepository: QuranRepository,
    historyRepository: ReadingHistoryRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        quranRepository.observeSurah().filterNotNull(),
        quranRepository.observeVerses().map { verses -> verses.firstOrNull()?.juz },
        historyRepository.observeLastRead(),
    ) { surah, juz, lastRead -> HomeUiState(surah, juz, lastRead) }
        .catch { emit(HomeUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
