package com.example.invyucab_project.mainui.locationsearchscreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.Prediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// This data class will now be built from API results
data class SearchLocation(
    val name: String,
    val address: String,
    val icon: ImageVector,
    val placeId: String // To pass to the next screen
)

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val apiService: GoogleMapsApiService // INJECTED
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    // Replaced Dummy list with a StateFlow for search results
    private val _searchResults = MutableStateFlow<List<SearchLocation>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null
    private var sessionToken: String = UUID.randomUUID().toString() // For Places API billing

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel() // Cancel previous job

        if (query.length < 3) {
            _searchResults.value = emptyList() // Clear results if query is too short
            return
        }

        // Start a new coroutine with a 300ms delay (debounce)
        searchJob = viewModelScope.launch {
            delay(300)
            try {
                val response = apiService.getPlaceAutocomplete(query, sessionToken)
                if (response.status == "OK") {
                    _searchResults.value = response.predictions.map { it.toSearchLocation() }
                }
            } catch (e: Exception) {
                // TODO: Handle error
                e.printStackTrace()
            }
        }
    }

    // Helper function to reset the session token when needed (e.g., on screen exit)
    fun resetSessionToken() {
        sessionToken = UUID.randomUUID().toString()
    }
}

// Helper to convert API response to our UI model
fun Prediction.toSearchLocation(): SearchLocation {
    return SearchLocation(
        name = this.structuredFormatting.mainText,
        address = this.structuredFormatting.secondaryText,
        icon = Icons.Default.LocationOn, // Use a standard icon for results
        placeId = this.placeId
    )
}