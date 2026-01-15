package com.example.vptapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.vptapp.navigation.MainScreen
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize Places API with the API key from Manifest
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
