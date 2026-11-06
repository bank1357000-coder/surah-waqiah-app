package com.alwaqiah.quran.presentation.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.model.Revelation
import com.alwaqiah.quran.presentation.components.LoadingState
import com.alwaqiah.quran.presentation.components.WaqiahTopBar

@Composable
fun SurahInfoScreen(onBack: () -> Unit, viewModel: SurahInfoViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { WaqiahTopBar(stringResource(R.string.feature_info), onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val surah = state.surah
        if (surah == null) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp).testTag("surah-info-screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow(stringResource(R.string.info_name), "${surah.nameFa} (${surah.nameArabic})", "info-name")
                    InfoRow(stringResource(R.string.info_number), surah.number.toPersianDigits(), "info-number")
                    InfoRow(
                        stringResource(R.string.info_revelation),
                        stringResource(if (surah.revelation == Revelation.MECCAN) R.string.surah_meccan else R.string.surah_medinan),
                        "info-revelation",
                    )
                    InfoRow(stringResource(R.string.info_verses), surah.verseCount.toPersianDigits(), "info-verses")
                    InfoRow(stringResource(R.string.info_juz), state.juzList.joinToString("، ") { it.toPersianDigits() }, "info-juz")
                    if (state.firstPage != null && state.lastPage != null) {
                        InfoRow(
                            stringResource(R.string.info_pages),
                            stringResource(R.string.info_pages_value, state.firstPage!!.toPersianDigits(), state.lastPage!!.toPersianDigits()),
                            "info-pages",
                            showDivider = false,
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.info_source_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("info-source-note"),
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, tag: String, showDivider: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag(tag)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
    if (showDivider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
