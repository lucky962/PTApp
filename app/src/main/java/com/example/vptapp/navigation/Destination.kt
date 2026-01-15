package com.example.vptapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val isTopLevel: Boolean = true
) {
    FORYOU("foryou", "For You", Icons.Default.Home, "For You"),
    SEARCH("search", "Search", Icons.Default.Search, "Search"),
    OPTIONS("options", "Options", Icons.Default.Menu, "Options"),

    ABOUT("about", "About", Icons.Default.Info, "About", isTopLevel = false),
    NOTIFICATIONS("notifications", "Notifications", Icons.Default.Notifications, "Notifications", isTopLevel = false),
    PREFERENCES("preferences", "App Preferences", Icons.Default.Settings, "App Preferences", isTopLevel = false)
}
