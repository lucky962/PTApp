package com.example.vptapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vptapp.data.PlacesRepository
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class AutocompleteState(
    val predictions: List<AutocompletePrediction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AutocompleteViewModel(private val repository: PlacesRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow(AutocompleteState())
    val state: StateFlow<AutocompleteState> = _state.asStateFlow()

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace: StateFlow<Place?> = _selectedPlace.asStateFlow()

    init {
        setupQueryDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupQueryDebounce() {
        _query
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            _state.value = AutocompleteState()
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val predictions = repository.getAutocompletePredictions(query)
            _state.value = AutocompleteState(predictions = predictions)
        } catch (e: Exception) {
            _state.value = AutocompleteState(error = e.message)
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
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val place = repository.getPlaceDetails(placeId)
            if (place != null) {
                _selectedPlace.value = place
                _state.value = AutocompleteState()
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Failed to fetch details")
            }
        }
    }
}
