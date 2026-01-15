package com.example.vptapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AutocompleteState(
    val predictions: List<AutocompletePrediction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AutocompleteViewModel : ViewModel() {
    private lateinit var placesClient: PlacesClient

    private val _state = MutableStateFlow(AutocompleteState())
    val state: StateFlow<AutocompleteState> = _state.asStateFlow()

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace: StateFlow<Place?> = _selectedPlace.asStateFlow()

    fun initialize(context: Context) {
        if (!::placesClient.isInitialized) {
            placesClient = Places.createClient(context)
        }
    }

    fun onQueryChanged(query: String) {
        if (query.isBlank()) {
            _state.value = AutocompleteState()
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("AU") // Restrict to Australia
                .build()

            try {
                val response = placesClient.findAutocompletePredictions(request).await()
                _state.value = AutocompleteState(predictions = response.autocompletePredictions)
            } catch (e: Exception) {
                _state.value = AutocompleteState(error = e.message)
            }
        }
    }

    fun onPredictionSelected(prediction: AutocompletePrediction) {
        fetchPlaceDetails(prediction.placeId)
    }

    fun onPoiSelected(poi: PointOfInterest) {
        fetchPlaceDetails(poi.placeId)
    }

    fun clearSelectedPlace() {
        _selectedPlace.value = null
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
            Place.Field.TYPES
        )
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        viewModelScope.launch {
            try {
                val response = placesClient.fetchPlace(request).await()
                _selectedPlace.value = response.place
                _state.value = AutocompleteState() // Clear predictions
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
