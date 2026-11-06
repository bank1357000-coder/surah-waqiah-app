package com.alwaqiah.quran.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alwaqiah.quran.R
import com.alwaqiah.quran.core.util.playStoreUrl
import com.alwaqiah.quran.core.util.shareText
import com.alwaqiah.quran.core.util.toPersianDigits
import com.alwaqiah.quran.domain.model.Revelation
import com.alwaqiah.quran.presentation.components.drawEightPointStar
import com.alwaqiah.quran.presentation.navigation.AboutRoute
import com.alwaqiah.quran.presentation.navigation.BookmarksRoute
import com.alwaqiah.quran.presentation.navigation.ContactRoute
import com.alwaqiah.quran.presentation.navigation.MemorizationRoute
import com.alwaqiah.quran.presentation.navigation.OtherAppsRoute
import com.alwaqiah.quran.presentation.navigation.PlayerRoute
import com.alwaqiah.quran.presentation.navigation.ReaderRoute
import com.alwaqiah.quran.presentation.navigation.Route
import com.alwaqiah.quran.presentation.navigation.SearchRoute
import com.alwaqiah.quran.presentation.navigation.SettingsRoute
import com.alwaqiah.quran.presentation.navigation.SurahInfoRoute
import com.alwaqiah.quran.presentation.navigation.TafsirRoute
import com.alwaqiah.quran.presentation.navigation.VirtuesRoute
import com.alwaqiah.quran.presentation.theme.QuranFonts
import com.alwaqiah.quran.presentation.theme.WaqiahThemeExtras

private data class Feature(
    val tag: String,
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int?,
    val route: Route?,
)

@Composable
fun HomeScreen(onNavigate: (Route) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_app_title)
    val shareBody = stringResource(R.string.share_app_text, context.playStoreUrl())

    val quranFeatures = remember {
        listOf(
            Feature("reader", Icons.AutoMirrored.Filled.MenuBook, R.string.feature_reader, R.string.feature_reader_desc, ReaderRoute()),
            Feature("player", Icons.Filled.Headphones, R.string.feature_player, R.string.feature_player_desc, PlayerRoute),
            Feature("memorization", Icons.Filled.Repeat, R.string.feature_memorization, R.string.feature_memorization_desc, MemorizationRoute),
            Feature("tafsir", Icons.Filled.AutoStories, R.string.feature_tafsir, R.string.feature_tafsir_desc, TafsirRoute),
        )
    }
    val surahFeatures = remember {
        listOf(
            Feature("info", Icons.Filled.Info, R.string.feature_info, R.string.feature_info_desc, SurahInfoRoute),
            Feature("virtues", Icons.Filled.WorkspacePremium, R.string.feature_virtues, R.string.feature_virtues_desc, VirtuesRoute),
        )
    }
    val toolFeatures = remember {
        listOf(
            Feature("bookmarks", Icons.Filled.Bookmarks, R.string.feature_bookmarks, R.string.feature_bookmarks_desc, BookmarksRoute),
            Feature("search", Icons.Filled.Search, R.string.feature_search, R.string.feature_search_desc, SearchRoute),
            Feature("settings", Icons.Filled.Settings, R.string.feature_settings, R.string.feature_settings_desc, SettingsRoute),
        )
    }
    val appFeatures = remember {
        listOf(
            Feature("about", Icons.Filled.People, R.string.feature_about, null, AboutRoute),
            Feature("other-apps", Icons.Filled.Apps, R.string.feature_other_apps, null, OtherAppsRoute),
            Feature("contact", Icons.Filled.Email, R.string.feature_contact, null, ContactRoute),
            Feature("share-app", Icons.Filled.Share, R.string.feature_share_app, null, null),
        )
    }
    val onFeature: (Feature) -> Unit = { feature ->
        val route = feature.route
        if (route != null) onNavigate(route) else context.shareText(shareBody, shareTitle)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home-screen"),
        contentPadding = WindowInsets.safeDrawing.asPaddingValues().let {
            androidx.compose.foundation.layout.PaddingValues(
                top = it.calculateTopPadding() + 16.dp,
                bottom = it.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp,
            )
        },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") {
            HeroCard(
                state = state,
                onRead = { onNavigate(ReaderRoute(startVerse = state.lastRead?.verseNumber ?: 0)) },
            )
        }
        featureSection(R.string.home_section_quran, quranFeatures, columns = 2, onFeature)
        featureSection(R.string.home_section_surah, surahFeatures, columns = 2, onFeature)
        featureSection(R.string.home_section_tools, toolFeatures, columns = 3, onFeature)
        featureSection(R.string.home_section_app, appFeatures, columns = 2, onFeature)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.featureSection(
    titleRes: Int,
    features: List<Feature>,
    columns: Int,
    onFeature: (Feature) -> Unit,
) {
    item(key = "section-$titleRes") {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
    }
    features.chunked(columns).forEachIndexed { index, rowItems ->
        item(key = "row-$titleRes-$index") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { feature ->
                    FeatureTile(feature, onClick = { onFeature(feature) }, modifier = Modifier.weight(1f))
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun HeroCard(state: HomeUiState, onRead: () -> Unit) {
    val extra = WaqiahThemeExtras.colors
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(500)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 24.dp.toPx()
            }
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(extra.heroStart, extra.heroEnd)))
            .testTag("home-hero-card"),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val r = size.height * 0.55f
            drawEightPointStar(Offset(size.width * 0.12f, size.height * 0.2f), r, extra.gold.copy(alpha = 0.10f), Stroke(1.5.dp.toPx()))
            drawEightPointStar(Offset(size.width * 0.12f, size.height * 0.2f), r * 0.7f, extra.gold.copy(alpha = 0.08f), Stroke(1.dp.toPx()))
        }
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.labelLarge,
                color = extra.gold,
            )
            Text(
                text = state.surah?.nameArabic ?: "",
                fontFamily = QuranFonts.Hafs,
                fontSize = 44.sp,
                lineHeight = 72.sp,
                color = extra.onHero,
                modifier = Modifier.testTag("home-surah-name"),
            )
            state.surah?.let { surah ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill(stringResource(R.string.surah_number, surah.number.toPersianDigits()))
                    MetaPill(
                        stringResource(if (surah.revelation == Revelation.MECCAN) R.string.surah_meccan else R.string.surah_medinan)
                    )
                    MetaPill(stringResource(R.string.surah_verse_count, surah.verseCount.toPersianDigits()))
                    state.juz?.let { MetaPill(stringResource(R.string.juz_label, it.toPersianDigits())) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onRead,
                colors = ButtonDefaults.buttonColors(containerColor = extra.gold, contentColor = extra.heroStart),
                modifier = Modifier.height(48.dp).testTag("home-read-button"),
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                val last = state.lastRead
                Text(
                    if (last != null) stringResource(R.string.home_continue_reading, last.verseNumber.toPersianDigits())
                    else stringResource(R.string.home_start_reading)
                )
            }
        }
    }
}

@Composable
private fun MetaPill(text: String) {
    val extra = WaqiahThemeExtras.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = extra.onHero,
        modifier = Modifier
            .clip(CircleShape)
            .background(extra.onHero.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun FeatureTile(feature: Feature, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "tile-scale")

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.large)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick)
            .testTag("home-feature-${feature.tag}"),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(feature.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(
                stringResource(feature.titleRes),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            feature.descRes?.let {
                Text(
                    stringResource(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
