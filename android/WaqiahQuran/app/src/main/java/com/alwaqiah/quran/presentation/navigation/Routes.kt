package com.alwaqiah.quran.presentation.navigation

import kotlinx.serialization.Serializable

/** Type-safe Navigation Compose destinations. */
sealed interface Route

@Serializable data object HomeRoute : Route
@Serializable data class ReaderRoute(val startVerse: Int = 0) : Route
@Serializable data object PlayerRoute : Route
@Serializable data object MemorizationRoute : Route
@Serializable data object TafsirRoute : Route
@Serializable data object SurahInfoRoute : Route
@Serializable data object VirtuesRoute : Route
@Serializable data object BookmarksRoute : Route
@Serializable data object SearchRoute : Route
@Serializable data object SettingsRoute : Route
@Serializable data object AboutRoute : Route
@Serializable data object OtherAppsRoute : Route
@Serializable data object ContactRoute : Route
