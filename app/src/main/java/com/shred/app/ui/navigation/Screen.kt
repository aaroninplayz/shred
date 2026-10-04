package com.shred.app.ui.navigation

import android.net.Uri
import com.shred.app.data.model.MediaType

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Favorites : Screen("favorites")

    object FolderBrowser : Screen("folders/{mediaType}") {
        fun createRoute(mediaType: MediaType) = "folders/${mediaType.name}"
    }

    object Swipe : Screen("swipe/{mediaType}?folderPath={folderPath}") {
        fun createRoute(mediaType: MediaType, folderPath: String? = null): String {
            return if (!folderPath.isNullOrEmpty()) {
                "swipe/${mediaType.name}?folderPath=${Uri.encode(folderPath)}"
            } else {
                "swipe/${mediaType.name}"
            }
        }
    }

    object Kept : Screen("kept/{mediaType}?folderPath={folderPath}") {
        fun createRoute(mediaType: MediaType, folderPath: String? = null): String {
            return if (!folderPath.isNullOrEmpty()) {
                "kept/${mediaType.name}?folderPath=${Uri.encode(folderPath)}"
            } else {
                "kept/${mediaType.name}"
            }
        }
    }

    object Trash : Screen("trash")
}
