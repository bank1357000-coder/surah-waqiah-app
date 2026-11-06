package com.alwaqiah.quran.presentation.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alwaqiah.quran.domain.model.SurahInfo
import com.alwaqiah.quran.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SurahInfoUiState(
    val surah: SurahInfo? = null,
    val juzList: List<Int> = emptyList(),
    val firstPage: Int? = null,
    val lastPage: Int? = null,
)

/** Shows only facts derived from the bundled, verified metadata — nothing hand-written. */
@HiltViewModel
class SurahInfoViewModel @Inject constructor(quranRepository: QuranRepository) : ViewModel() {
    val uiState: StateFlow<SurahInfoUiState> = combine(
        quranRepository.observeSurah().filterNotNull(),
        quranRepository.observeVerses(),
    ) { surah, verses ->
        SurahInfoUiState(
            surah = surah,
            juzList = verses.map { it.juz }.distinct(),
            firstPage = verses.minOfOrNull { it.page },
            lastPage = verses.maxOfOrNull { it.page },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SurahInfoUiState())
}
