package com.shred.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shred.app.ShredApplication
import com.shred.app.data.model.MediaType
import com.shred.app.ui.screens.favorites.FavoritesScreen
import com.shred.app.ui.screens.favorites.FavoritesViewModel
import com.shred.app.ui.screens.home.HomeScreen
import com.shred.app.ui.screens.home.HomeViewModel
import com.shred.app.ui.screens.loading.LoadingScreen
import com.shred.app.ui.screens.loading.LoadingViewModel
import com.shred.app.ui.screens.settings.SettingsScreen
import com.shred.app.ui.screens.settings.SettingsViewModel
import com.shred.app.ui.screens.swipe.SwipeScreen
import com.shred.app.ui.screens.swipe.SwipeViewModel

@Composable
fun ShredNavGraph(
    navController: NavHostController,
    app: ShredApplication
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route,
        enterTransition = { fadeIn(tween(250)) },
        exitTransition = { fadeOut(tween(250)) }
    ) {
        // 1. Cinematic Loading Screen
        composable(Screen.Loading.route) {
            val viewModel = LoadingViewModel(
                scanner = app.scanner,
                settingsRepository = app.settingsRepository,
                mediaRepository = app.mediaRepository
            )
            LoadingScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Shredder Machine Home Screen
        composable(Screen.Home.route) {
            val viewModel = HomeViewModel(
                mediaRepository = app.mediaRepository,
                settingsRepository = app.settingsRepository
            )
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSwipe = { mediaType ->
                    navController.navigate(Screen.Swipe.createRoute(mediaType))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // 3. Swipe Screen
        composable(
            route = Screen.Swipe.route,
            arguments = listOf(navArgument("mediaType") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("mediaType") ?: MediaType.PHOTO.name
            val mediaType = try {
                MediaType.valueOf(typeStr)
            } catch (e: Exception) {
                MediaType.PHOTO
            }

            val viewModel = SwipeViewModel(
                mediaType = mediaType,
                mediaRepository = app.mediaRepository,
                settingsRepository = app.settingsRepository,
                trashManager = app.trashManager,
                hapticHelper = app.hapticHelper,
                soundHelper = app.soundHelper
            )

            SwipeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Favorites Vault Screen
        composable(Screen.Favorites.route) {
            val viewModel = FavoritesViewModel(
                mediaRepository = app.mediaRepository,
                settingsRepository = app.settingsRepository,
                trashManager = app.trashManager,
                hapticHelper = app.hapticHelper,
                soundHelper = app.soundHelper
            )
            FavoritesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Settings Screen
        composable(Screen.Settings.route) {
            val viewModel = SettingsViewModel(
                settingsRepository = app.settingsRepository,
                mediaRepository = app.mediaRepository
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
