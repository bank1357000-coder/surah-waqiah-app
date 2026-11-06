package com.alwaqiah.quran.presentation.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alwaqiah.quran.BuildConfig
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.composeEmail
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.presentation.components.MessageState
import com.alwaqiah.quran.presentation.components.WaqiahTopBar
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { WaqiahTopBar(stringResource(R.string.feature_about), onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp).testTag("about-screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(stringResource(R.string.app_name), stringResource(R.string.about_body))
            Card(stringResource(R.string.about_sources_title), stringResource(R.string.about_sources_body))
            Text(
                stringResource(R.string.about_version, BuildConfig.VERSION_NAME.toPersianDigits()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("about-version"),
            )
        }
    }
}

@Composable
private fun Card(title: String, body: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ContactScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val email = stringResource(R.string.contact_email)
    val subject = stringResource(R.string.contact_email_subject)
    Scaffold(
        topBar = { WaqiahTopBar(stringResource(R.string.feature_contact), onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        MessageState(
            icon = Icons.Filled.Email,
            title = email,
            body = stringResource(R.string.contact_body),
            modifier = Modifier.padding(padding).testTag("contact-screen"),
            action = {
                Button(
                    onClick = { if (!context.composeEmail(email, subject)) scope.launch { snackbar.showSnackbar(email) } },
                    modifier = Modifier.testTag("contact-send-email-button"),
                ) { Text(stringResource(R.string.contact_send_email)) }
            },
        )
    }
}

@Composable
fun OtherAppsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { WaqiahTopBar(stringResource(R.string.feature_other_apps), onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        MessageState(
            icon = Icons.Filled.Apps,
            title = stringResource(R.string.other_apps_empty_title),
            body = stringResource(R.string.other_apps_empty_body),
            modifier = Modifier.padding(padding).testTag("other-apps-screen"),
        )
    }
}
