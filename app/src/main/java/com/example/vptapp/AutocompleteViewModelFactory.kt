package com.example.vptapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vptapp.data.PlacesRepository

class AutocompleteViewModelFactory(private val repository: PlacesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AutocompleteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AutocompleteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
