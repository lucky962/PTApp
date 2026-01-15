package com.example.vptapp.data

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await

class PlacesRepository(private val placesClient: PlacesClient) {

    suspend fun getAutocompletePredictions(query: String): List<AutocompletePrediction> {
        if (query.isBlank()) return emptyList()

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("AU")
            .build()

        return try {
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPlaceDetails(placeId: String): Place? {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
            Place.Field.TYPES
        )
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        return try {
            val response = placesClient.fetchPlace(request).await()
            response.place
        } catch (e: Exception) {
            null
        }
    }
}
