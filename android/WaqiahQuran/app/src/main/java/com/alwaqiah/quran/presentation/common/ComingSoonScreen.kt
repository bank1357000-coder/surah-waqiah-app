package com.alwaqiah.quran.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.alwaqiah.quran.R
import com.alwaqiah.quran.presentation.components.MessageState
import com.alwaqiah.quran.presentation.components.WaqiahTopBar

/** Honest placeholder for features scheduled in later phases. */
@Composable
fun ComingSoonScreen(title: String, phase: String, description: String, onBack: () -> Unit) {
    Scaffold(topBar = { WaqiahTopBar(title = title, onBack = onBack) }) { padding ->
        MessageState(
            icon = Icons.Outlined.Construction,
            title = stringResource(R.string.coming_soon_phase, phase),
            body = description,
            modifier = Modifier.padding(padding).testTag("coming-soon-screen"),
        )
    }
}
