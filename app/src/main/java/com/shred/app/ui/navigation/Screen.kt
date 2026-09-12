package com.shred.app.ui.navigation

import com.shred.app.data.model.MediaType

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Favorites : Screen("favorites")

    object Swipe : Screen("swipe/{mediaType}") {
        fun createRoute(mediaType: MediaType) = "swipe/${mediaType.name}"
    }
}
