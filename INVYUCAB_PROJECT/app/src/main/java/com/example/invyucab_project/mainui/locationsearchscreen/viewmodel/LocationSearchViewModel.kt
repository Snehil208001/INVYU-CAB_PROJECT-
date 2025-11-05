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

// ✅ ADDED: Enum to track which field is being edited
enum class EditingField {
    PICKUP,
    DROP
}

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val apiService: GoogleMapsApiService // INJECTED
) : ViewModel() {

    // ✅ MODIFIED: State for the search query itself
    var searchQuery by mutableStateOf("")
        private set

    // ✅ ADDED: State for pickup field
    var pickupDescription by mutableStateOf("Your Current Location")
        private set
    var pickupPlaceId by mutableStateOf<String?>("current_location") // Special key for "current"
        private set

    // ✅ ADDED: State for drop field
    var dropDescription by mutableStateOf("")
        private set
    var dropPlaceId by mutableStateOf<String?>(null)
        private set

    // ✅ ADDED: State for active field
    var activeField by mutableStateOf(EditingField.DROP)
        private set

    // Replaced Dummy list with a StateFlow for search results
    private val _searchResults = MutableStateFlow<List<SearchLocation>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null
    private var sessionToken: String = UUID.randomUUID().toString() // For Places API billing

    // ✅✅✅ THIS IS THE CORRECTION ✅✅✅
    // This now only updates the search query, not the description fields.
    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel() // Cancel previous job

        // ✅ ADDED: Clear the placeId if the user starts typing again,
        // indicating they are changing their selection.
        if (activeField == EditingField.PICKUP && query != pickupDescription) {
            pickupPlaceId = null
        } else if (activeField == EditingField.DROP && query != dropDescription) {
            dropPlaceId = null
        }

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

    // ✅ ADDED: Function to set the active field
    fun onFieldActivated(field: EditingField) {
        activeField = field
        // Set search query to the field's current content (unless it's the default)
        searchQuery = when (field) {
            EditingField.PICKUP -> if (pickupPlaceId == "current_location") "" else pickupDescription
            EditingField.DROP -> dropDescription
        }
        _searchResults.value = emptyList() // Clear results when switching
    }

    // ✅ MODIFIED: Function to handle when a search result is clicked
    fun onSearchResultClicked(location: SearchLocation) {
        if (activeField == EditingField.PICKUP) {
            pickupDescription = location.name
            pickupPlaceId = location.placeId
            // Move focus to DROP field next
            activeField = EditingField.DROP
            searchQuery = "" // ✅ ADDED: Clear search query
        } else {
            dropDescription = location.name
            dropPlaceId = location.placeId
            // Move focus to PICKUP field if it's not set
            if (pickupPlaceId == null) {
                activeField = EditingField.PICKUP
                searchQuery = "" // ✅ ADDED: Clear search query
            } else {
                // Both fields are set, just clear search query
                searchQuery = "" // ✅ ADDED: Clear search query
            }
        }
        _searchResults.value = emptyList() // Clear results
        resetSessionToken() // Reset token after a selection
    }


    // Helper function to reset the session token when needed
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