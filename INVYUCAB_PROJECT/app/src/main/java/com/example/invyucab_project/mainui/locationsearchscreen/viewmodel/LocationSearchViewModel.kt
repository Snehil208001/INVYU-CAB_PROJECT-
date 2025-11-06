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

    // ✅ State for pickup field
    var pickupDescription by mutableStateOf("Your Current Location")
        private set
    var pickupPlaceId by mutableStateOf<String?>("current_location") // Special key for "current"
        private set

    // ✅ State for drop field
    var dropDescription by mutableStateOf("")
        private set
    var dropPlaceId by mutableStateOf<String?>(null)
        private set

    // ✅ State for active field
    var activeField by mutableStateOf(EditingField.DROP)
        private set

    // StateFlow for search results
    private val _searchResults = MutableStateFlow<List<SearchLocation>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null
    private var sessionToken: String = UUID.randomUUID().toString() // For Places API billing

    // This now updates the *active field's* description and triggers a search.
    fun onQueryChanged(query: String) {
        searchJob?.cancel() // Cancel previous job

        // Update the correct field's text and clear its ID
        if (activeField == EditingField.PICKUP) {
            pickupDescription = query
            // ✅ MODIFIED: Only clear placeId if the query is not the default text
            if (query != "Your Current Location") pickupPlaceId = null
        } else {
            dropDescription = query
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

    // ✅ Function to set the active field
    fun onFieldActivated(field: EditingField) {
        activeField = field
        // When activating "Current Location", clear text to start a new search
        if (field == EditingField.PICKUP && pickupPlaceId == "current_location") {
            pickupDescription = ""
            pickupPlaceId = null
        }
        _searchResults.value = emptyList() // Clear results when switching
    }

    // ✅✅✅ NEW FUNCTION ✅✅✅
    // This is the fix you requested.
    fun onFieldFocusLost(field: EditingField) {
        if (field == EditingField.PICKUP) {
            if (pickupDescription.isBlank()) {
                pickupDescription = "Your Current Location"
                pickupPlaceId = "current_location"
            }
        }
    }

    // ✅ Function to handle when a search result is clicked
    fun onSearchResultClicked(location: SearchLocation) {
        if (activeField == EditingField.PICKUP) {
            pickupDescription = location.name
            pickupPlaceId = location.placeId
            // Move focus to DROP field next
            activeField = EditingField.DROP
        } else {
            dropDescription = location.name
            dropPlaceId = location.placeId
            // Move focus to PICKUP field if it's not set
            if (pickupPlaceId == null) {
                activeField = EditingField.PICKUP
            }
        }
        _searchResults.value = emptyList() // Clear results
        resetSessionToken() // Reset token after a selection
    }

    // ✅ New function to clear text from a field
    fun onClearField(field: EditingField) {
        if (field == EditingField.PICKUP) {
            pickupDescription = ""
            pickupPlaceId = null
        } else {
            dropDescription = ""
            dropPlaceId = null
        }
        _searchResults.value = emptyList()
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