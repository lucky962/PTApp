package com.example.vptapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize Places API with the API key from BuildConfig or Manifest
        val apiKey = packageManager.getApplicationInfo(packageName, android.content.pm.PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY") ?: ""
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }

        setContent {
            MaterialTheme {
                Surface {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun ForYouScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("For You Screen")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(viewModel: AutocompleteViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val autocompleteState by viewModel.state.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val locationPermissionsState = rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )

        // Request permissions when the screen loads
        LaunchedEffect(Unit) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }

        val locationPermissionGranted = locationPermissionsState.permissions.any { it.status.isGranted }

        val melbourne = LatLng(-37.811094555577014, 144.97148748731263)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(melbourne, 12f)
        }

        // Animate camera to selected location
        LaunchedEffect(selectedPlace) {
            selectedPlace?.location?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = locationPermissionGranted),
            onPOIClick = { poi ->
                viewModel.onPoiSelected(poi)
            },
            onMapClick = {
                viewModel.clearSelectedPlace()
            }
        ) {
            selectedPlace?.location?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = selectedPlace?.displayName
                )
            }
        }

        DockedSearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            expanded = active,
            onExpandedChange = { active = it },
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.onQueryChanged(it)
                    },
                    onSearch = { active = false },
                    expanded = active,
                    onExpandedChange = { active = it },
                    placeholder = { Text("Search location...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.onQueryChanged("")
                                active = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }
        ) {
            if (autocompleteState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (autocompleteState.error != null) {
                ListItem(
                    headlineContent = { Text("Error", color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text(autocompleteState.error!!) },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(autocompleteState.predictions) { prediction ->
                    val icon = when {
                        prediction.types.any { it == "train_station"} -> Icons.Default.Train
                        prediction.types.any { it == "bus_stop" || it == "bus_station" } -> Icons.Default.DirectionsBus
                        else -> Icons.Default.Place
                    }

                    ListItem(
                        headlineContent = { Text(prediction.getPrimaryText(null).toString()) },
                        supportingContent = { Text(prediction.getSecondaryText(null).toString()) },
                        leadingContent = { Icon(icon, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.onPredictionSelected(prediction)
                            searchQuery = prediction.getFullText(null).toString()
                            active = false
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        // Place Details Bottom Sheet
        if (selectedPlace != null) {
            val sheetSnackbarHostState = remember { SnackbarHostState() }
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelectedPlace() },
                sheetState = sheetState,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 12.dp)
                            .size(32.dp, 4.dp)
                            .background(Color.LightGray, RoundedCornerShape(2.dp))
                    )
                }
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    PlaceDetailsContent(
                        place = selectedPlace!!,
                        onGetDirections = {
                            scope.launch {
                                sheetSnackbarHostState.showSnackbar("Feature coming soon")
                            }
                        },
                        onNearbyTransit = {
                            scope.launch {
                                sheetSnackbarHostState.showSnackbar("Feature coming soon")
                            }
                        }
                    )
                    SnackbarHost(
                        hostState = sheetSnackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceDetailsContent(
    place: Place,
    onGetDirections: () -> Unit,
    onNearbyTransit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
    ) {
        Text(
            text = place.displayName ?: "Unknown Location",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = place.formattedAddress ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onGetDirections,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Directions, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Directions")
            }
            
            Button(
                onClick = onNearbyTransit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.DirectionsTransit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transit")
            }
        }
    }
}

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
            // A nice title for the page
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
                // 1. App Icon - Rounded and clipped directly
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.FillBounds
                )

                Spacer(modifier = Modifier.padding(12.dp))

                // 2. App Name and Version
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

                // 3. Description
                Text(
                    text = "This app provides public transport information. At this point of time Australian public transport is planned starting with Melbourne public transport. This is a hobby project so please be nice. :)",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f)) // Pushes copyright to bottom

                // 4. Footer
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(Destination.FORYOU.route) { ForYouScreen() }
        composable(Destination.SEARCH.route) { SearchScreen() }
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

@Preview
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.FORYOU
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.filter { it.isTopLevel }.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.route) {
                                // Standard navigation practice for BottomBars
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            selectedDestination = index
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
