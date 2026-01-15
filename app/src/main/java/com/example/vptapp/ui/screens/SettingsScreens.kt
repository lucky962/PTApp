package com.example.vptapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vptapp.R
import com.example.vptapp.SettingsManager
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun OptionsScreen(
    modifier: Modifier = Modifier,
    onNavigateToAbout: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPreferences: () -> Unit = {}) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn {
                item {
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Sound, vibration & priority"
                    ) {
                        onNavigateToNotifications()
                    }
                }

                item {
                    SettingsRow(
                        icon = Icons.Default.Settings,
                        title = "App Preferences",
                        subtitle = "Theme, language, and data usage"
                    ) {
                        onNavigateToPreferences()
                    }
                }

                item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

                item {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "Version 1.0.0"
                    ) {
                        onNavigateToAbout()
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
         },
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.FillBounds
                )

                Spacer(modifier = Modifier.padding(12.dp))

                Text(
                    text = "VPT App",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.padding(24.dp))

                Text(
                    text = "This app provides public transport information. At this point of time Australian public transport is planned starting with Melbourne public transport. This is a hobby project so please be nice. :)",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "© 2025 Lucky962's Apps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit = {}
) {
    val notificationsEnabled by settingsManager.notificationsEnabled.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Enable Notifications") },
                supportingContent = { Text("Receive updates and alerts") },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            coroutineScope.launch {
                                settingsManager.setNotificationsEnabled(enabled)
                            }
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPreferencesScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit = {}
) {
    val adsEnabled by settingsManager.adsEnabled.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Enable Ads") },
                supportingContent = { Text("Show personalized ads in the app") },
                trailingContent = {
                    Switch(
                        checked = adsEnabled,
                        onCheckedChange = { enabled ->
                            coroutineScope.launch {
                                settingsManager.setAdsEnabled(enabled)
                            }
                        }
                    )
                }
            )
        }
    }
}
