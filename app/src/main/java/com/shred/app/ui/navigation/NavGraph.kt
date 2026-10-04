package com.shred.app.ui.navigation

import android.net.Uri
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
import com.shred.app.ui.screens.folders.FolderBrowserScreen
import com.shred.app.ui.screens.folders.FolderBrowserViewModel
import com.shred.app.ui.screens.home.HomeScreen
import com.shred.app.ui.screens.home.HomeViewModel
import com.shred.app.ui.screens.kept.KeptScreen
import com.shred.app.ui.screens.kept.KeptViewModel
import com.shred.app.ui.screens.settings.SettingsScreen
import com.shred.app.ui.screens.settings.SettingsViewModel
import com.shred.app.ui.screens.swipe.SwipeScreen
import com.shred.app.ui.screens.swipe.SwipeViewModel
import com.shred.app.ui.screens.trash.TrashScreen
import com.shred.app.ui.screens.trash.TrashViewModel

@Composable
fun ShredNavGraph(
    navController: NavHostController,
    app: ShredApplication
) {
    val animSpec = com.shred.app.ui.theme.LocalShredAnimation.current
    val duration = animSpec.transitionDurationMs

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(duration)) +
                scaleIn(initialScale = 0.94f, animationSpec = tween(duration))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(duration)) +
                scaleOut(targetScale = 1.06f, animationSpec = tween(duration))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(duration)) +
                scaleIn(initialScale = 1.06f, animationSpec = tween(duration))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(duration)) +
                scaleOut(targetScale = 0.94f, animationSpec = tween(duration))
        }
    ) {
        // 1. Home Screen (Level 1: Categories)
        composable(Screen.Home.route) {
            val viewModel = HomeViewModel(
                mediaRepository = app.mediaRepository,
                settingsRepository = app.settingsRepository,
                scanner = app.scanner
            )
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSwipe = { mediaType ->
                    // Level 1 -> Level 2: Navigates to Folder Browser
                    navController.navigate(Screen.FolderBrowser.createRoute(mediaType)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToTrash = {
                    navController.navigate(Screen.Trash.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 2. Folder Browser Screen (Level 2: Folders)
        composable(
            route = Screen.FolderBrowser.route,
            arguments = listOf(navArgument("mediaType") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("mediaType") ?: MediaType.PHOTO.name
            val mediaType = try {
                MediaType.valueOf(typeStr)
            } catch (e: Exception) {
                MediaType.PHOTO
            }

            val viewModel = FolderBrowserViewModel(
                mediaType = mediaType,
                mediaRepository = app.mediaRepository
            )

            FolderBrowserScreen(
                viewModel = viewModel,
                onNavigateToSwipe = { folderPath ->
                    // Level 2 -> Level 3: Navigates to Swipe with folder filter
                    navController.navigate(Screen.Swipe.createRoute(mediaType, folderPath)) {
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Swipe Screen (Level 3: Files within folder or entire media type)
        composable(
            route = Screen.Swipe.route,
            arguments = listOf(
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("folderPath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("mediaType") ?: MediaType.PHOTO.name
            val mediaType = try {
                MediaType.valueOf(typeStr)
            } catch (e: Exception) {
                MediaType.PHOTO
            }
            val rawFolderPath = backStackEntry.arguments?.getString("folderPath")
            val folderPath = if (!rawFolderPath.isNullOrEmpty()) Uri.decode(rawFolderPath) else null

            val viewModel = SwipeViewModel(
                mediaType = mediaType,
                folderPath = folderPath,
                mediaRepository = app.mediaRepository,
                settingsRepository = app.settingsRepository,
                trashManager = app.trashManager,
                hapticHelper = app.hapticHelper,
                soundHelper = app.soundHelper
            )

            SwipeScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToKept = {
                    navController.navigate(Screen.Kept.createRoute(mediaType, folderPath)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToTrash = {
                    navController.navigate(Screen.Trash.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 4. Favorites Vault Screen
        composable(
            route = Screen.Favorites.route,
            enterTransition = {
                fadeIn(animationSpec = tween(duration)) + scaleIn(initialScale = 0.95f, animationSpec = tween(duration))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(duration)) + scaleOut(targetScale = 1.05f, animationSpec = tween(duration))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(duration)) + scaleIn(initialScale = 1.05f, animationSpec = tween(duration))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(duration)) + scaleOut(targetScale = 0.95f, animationSpec = tween(duration))
            }
        ) {
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

        // 5. Kept Vault Screen
        composable(
            route = Screen.Kept.route,
            arguments = listOf(
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("folderPath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(duration)) + scaleIn(initialScale = 0.95f, animationSpec = tween(duration))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(duration)) + scaleOut(targetScale = 1.05f, animationSpec = tween(duration))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(duration)) + scaleIn(initialScale = 1.05f, animationSpec = tween(duration))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(duration)) + scaleOut(targetScale = 0.95f, animationSpec = tween(duration))
            }
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("mediaType") ?: MediaType.PHOTO.name
            val mediaType = try {
                MediaType.valueOf(typeStr)
            } catch (e: Exception) {
                MediaType.PHOTO
            }
            val rawFolderPath = backStackEntry.arguments?.getString("folderPath")
            val folderPath = if (!rawFolderPath.isNullOrEmpty()) Uri.decode(rawFolderPath) else null

            val viewModel = KeptViewModel(
                mediaType = mediaType,
                folderPath = folderPath,
                mediaRepository = app.mediaRepository,
                hapticHelper = app.hapticHelper,
                soundHelper = app.soundHelper
            )
            KeptScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Trash Vault Screen
        composable(
            route = Screen.Trash.route,
            enterTransition = {
                fadeIn(animationSpec = tween(duration)) + scaleIn(initialScale = 0.95f, animationSpec = tween(duration))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(duration)) + scaleOut(targetScale = 1.05f, animationSpec = tween(duration))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(duration)) + scaleIn(initialScale = 1.05f, animationSpec = tween(duration))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(duration)) + scaleOut(targetScale = 0.95f, animationSpec = tween(duration))
            }
        ) {
            val viewModel = TrashViewModel(
                mediaRepository = app.mediaRepository,
                trashManager = app.trashManager,
                settingsRepository = app.settingsRepository,
                hapticHelper = app.hapticHelper,
                soundHelper = app.soundHelper
            )
            TrashScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Settings Screen
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(duration)) + fadeIn(tween(duration))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(duration)) + fadeOut(tween(duration))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(duration))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(duration)) + fadeOut(tween(duration))
            }
        ) {
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
