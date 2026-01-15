package com.example.vptapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vptapp.AutocompleteViewModel
import com.example.vptapp.AutocompleteViewModelFactory
import com.example.vptapp.SettingsManager
import com.example.vptapp.data.PlacesRepository
import com.example.vptapp.ui.screens.*
import com.google.android.libraries.places.api.Places

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Manual dependency injection since we're not using Hilt
    val placesClient = remember { Places.createClient(context) }
    val placesRepository = remember { PlacesRepository(placesClient) }
    
    NavHost(
        navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(Destination.FORYOU.route) { ForYouScreen() }
        composable(Destination.SEARCH.route) {
            val autocompleteViewModel: AutocompleteViewModel = viewModel(
                factory = AutocompleteViewModelFactory(placesRepository)
            )
            SearchScreen(viewModel = autocompleteViewModel)
        }
        composable(Destination.OPTIONS.route) {
            OptionsScreen(
                onNavigateToAbout = { navController.navigate(Destination.ABOUT.route)},
                onNavigateToNotifications = { navController.navigate(Destination.NOTIFICATIONS.route) },
                onNavigateToPreferences = { navController.navigate(Destination.PREFERENCES.route) }
            )
        }

        composable(Destination.ABOUT.route) {
            AboutScreen (onBack = { navController.popBackStack() })
        }
        composable(Destination.NOTIFICATIONS.route) {
            NotificationsSettingsScreen(
                settingsManager = settingsManager,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Destination.PREFERENCES.route) {
            AppPreferencesScreen(
                settingsManager = settingsManager,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.FORYOU
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.filter { it.isTopLevel }.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            settingsManager = settingsManager,
            modifier = Modifier.padding(contentPadding)
        )
    }
}
