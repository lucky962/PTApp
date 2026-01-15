package com.example.vptapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vptapp.AutocompleteState
import com.example.vptapp.AutocompleteViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(viewModel: AutocompleteViewModel) {
    val autocompleteState by viewModel.state.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(Unit) {
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    val locationPermissionGranted = locationPermissionsState.permissions.any { it.status.isGranted }
    val melbourne = LatLng(-37.811094555577014, 144.97148748731263)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(melbourne, 12f)
    }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.location?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapContent(
            cameraPositionState = cameraPositionState,
            locationPermissionGranted = locationPermissionGranted,
            selectedPlace = selectedPlace,
            onPoiSelected = viewModel::onPoiSelected,
            onMapClick = viewModel::clearSelectedPlace
        )

        LocationSearchBar(
            autocompleteState = autocompleteState,
            onQueryChanged = viewModel::onQueryChanged,
            onPredictionSelected = viewModel::onPredictionSelected,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        if (selectedPlace != null) {
            PlaceDetailsSheet(
                place = selectedPlace!!,
                onDismiss = viewModel::clearSelectedPlace
            )
        }
    }
}

@Composable
fun MapContent(
    cameraPositionState: CameraPositionState,
    locationPermissionGranted: Boolean,
    selectedPlace: Place?,
    onPoiSelected: (com.google.android.gms.maps.model.PointOfInterest) -> Unit,
    onMapClick: () -> Unit
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
        uiSettings = MapUiSettings(myLocationButtonEnabled = locationPermissionGranted),
        onPOIClick = onPoiSelected,
        onMapClick = { onMapClick() }
    ) {
        selectedPlace?.location?.let {
            Marker(
                state = MarkerState(position = it),
                title = selectedPlace.displayName
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBar(
    autocompleteState: AutocompleteState,
    onQueryChanged: (String) -> Unit,
    onPredictionSelected: (AutocompletePrediction) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    DockedSearchBar(
        modifier = modifier.fillMaxWidth(),
        expanded = active,
        onExpandedChange = { active = it },
        inputField = {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    onQueryChanged(it)
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
                            onQueryChanged("")
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
                supportingContent = { Text(autocompleteState.error) },
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
                        onPredictionSelected(prediction)
                        searchQuery = prediction.getFullText(null).toString()
                        active = false
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsSheet(
    place: Place,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                place = place,
                onGetDirections = {
                    scope.launch { snackbarHostState.showSnackbar("Feature coming soon") }
                },
                onNearbyTransit = {
                    scope.launch { snackbarHostState.showSnackbar("Feature coming soon") }
                }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
