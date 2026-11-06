package com.alwaqiah.quran.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alwaqiah.quran.R
import com.alwaqiah.quran.presentation.about.AboutScreen
import com.alwaqiah.quran.presentation.about.ContactScreen
import com.alwaqiah.quran.presentation.about.OtherAppsScreen
import com.alwaqiah.quran.presentation.bookmarks.BookmarksScreen
import com.alwaqiah.quran.presentation.common.ComingSoonScreen
import com.alwaqiah.quran.presentation.home.HomeScreen
import com.alwaqiah.quran.presentation.info.SurahInfoScreen
import com.alwaqiah.quran.presentation.reader.ReaderScreen
import com.alwaqiah.quran.presentation.settings.SettingsScreen

@Composable
fun WaqiahNavHost(navController: NavHostController = rememberNavController()) {
    val back: () -> Unit = { navController.navigateUp() }
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        // In RTL, forward navigation slides in from the left edge.
        enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(280)) { -it / 10 } },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(240)) { -it / 10 } },
    ) {
        composable<HomeRoute> { HomeScreen(onNavigate = { route -> navController.navigate(route) }) }
        composable<ReaderRoute> { ReaderScreen(onBack = back) }
        composable<BookmarksRoute> {
            BookmarksScreen(onBack = back, onOpenVerse = { navController.navigate(ReaderRoute(startVerse = it)) })
        }
        composable<SettingsRoute> { SettingsScreen(onBack = back) }
        composable<SurahInfoRoute> { SurahInfoScreen(onBack = back) }
        composable<AboutRoute> { AboutScreen(onBack = back) }
        composable<ContactRoute> { ContactScreen(onBack = back) }
        composable<OtherAppsRoute> { OtherAppsScreen(onBack = back) }

        composable<PlayerRoute> {
            ComingSoonScreen(
                title = stringResource(R.string.feature_player),
                phase = stringResource(R.string.phase_2),
                description = stringResource(R.string.coming_soon_player),
                onBack = back,
            )
        }
        composable<MemorizationRoute> {
            ComingSoonScreen(
                title = stringResource(R.string.feature_memorization),
                phase = stringResource(R.string.phase_3),
                description = stringResource(R.string.coming_soon_memorization),
                onBack = back,
            )
        }
        composable<TafsirRoute> {
            ComingSoonScreen(
                title = stringResource(R.string.feature_tafsir),
                phase = stringResource(R.string.phase_4),
                description = stringResource(R.string.coming_soon_tafsir),
                onBack = back,
            )
        }
        composable<SearchRoute> {
            ComingSoonScreen(
                title = stringResource(R.string.feature_search),
                phase = stringResource(R.string.phase_4),
                description = stringResource(R.string.coming_soon_search),
                onBack = back,
            )
        }
        composable<VirtuesRoute> {
            ComingSoonScreen(
                title = stringResource(R.string.feature_virtues),
                phase = stringResource(R.string.phase_5),
                description = stringResource(R.string.coming_soon_virtues),
                onBack = back,
            )
        }
    }
}
