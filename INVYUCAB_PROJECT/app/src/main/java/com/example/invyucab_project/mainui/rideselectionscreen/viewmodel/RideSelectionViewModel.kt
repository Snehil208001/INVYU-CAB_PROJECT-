package com.example.invyucab_project.mainui.rideselectionscreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

data class RideOption(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val description: String,
    val price: Int
)

data class RideSelectionState(
    val pickupLocation: LatLng = LatLng(25.5941, 85.1376), // Default to Patna
    val dropLocation: LatLng? = null,
    val pickupDescription: String = "Your Current Location",
    val dropDescription: String = "",
    val routePolyline: List<LatLng> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RideSelectionViewModel @Inject constructor(
    private val apiService: GoogleMapsApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideSelectionState())
    val uiState = _uiState.asStateFlow()

    // TODO: Replace with real user location
    private val currentUserLocation = "25.594095, 85.137611" // Patna
    private val dropPlaceId: String = savedStateHandle.get<String>("placeId") ?: ""
    private val encodedDropDescription: String = savedStateHandle.get<String>("description") ?: ""

    // Decode the description
    private val dropDescription: String = try {
        URLDecoder.decode(encodedDropDescription, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        encodedDropDescription // Fallback
    }


    // Dummy ride options
    val rideOptions = listOf(
        RideOption(1, Icons.Default.TwoWheeler, "Bike", "2 mins away", 91),
        RideOption(2, Icons.Default.ElectricRickshaw, "Auto", "2 mins away", 148),
        RideOption(3, Icons.Default.LocalTaxi, "Cab Economy", "2 mins away", 217),
        RideOption(4, Icons.Default.Stars, "Cab Premium", "5 mins away", 274)
    )

    init {
        _uiState.value = _uiState.value.copy(dropDescription = dropDescription)
        fetchDirections()
    }

    private fun fetchDirections() {
        if (dropPlaceId.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No drop location selected")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // We use place IDs for accuracy
                // TODO: Replace origin with real current placeId
                val origin = "place_id:ChIJg-cT-jNhj4kRj8S2d32pQ2A" // Placeholder for Patna Junction
                val destination = "place_id:$dropPlaceId"

                val response = apiService.getDirections(origin = origin, destination = destination)

                if (response.status == "OK" && response.routes.isNotEmpty()) {
                    val points = response.routes[0].overviewPolyline.points
                    val decodedPolyline = PolyUtil.decode(points)

                    // TODO: Get drop location LatLng from Places Details API
                    // For now, let's just update the polyline
                    _uiState.value = _uiState.value.copy(
                        routePolyline = decodedPolyline,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.status,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "An unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
}