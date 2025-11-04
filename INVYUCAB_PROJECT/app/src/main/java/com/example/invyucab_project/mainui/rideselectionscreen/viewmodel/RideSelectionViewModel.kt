package com.example.invyucab_project.mainui.rideselectionscreen.viewmodel

import android.annotation.SuppressLint
import android.location.Location // ✅ ADDED Import
import android.os.Looper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.math.roundToInt

// ✅ MODIFIED RideOption to include subtitle and duration
data class RideOption(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val description: String, // ETA like "2 mins away"
    val price: Int,
    val subtitle: String? = null,
    val estimatedDurationMinutes: Int? = null // Trip duration
)

data class RideSelectionState(
    val pickupLocation: LatLng? = null, // ✅ Changed to nullable, default is now unknown
    val dropLocation: LatLng? = null,
    val pickupDescription: String = "Fetching current location...", // ✅ Updated default
    val dropDescription: String = "",
    val routePolyline: List<LatLng> = emptyList(),
    val tripDurationSeconds: Int? = null, // ✅ Added duration state
    val isLoading: Boolean = false,
    val isFetchingLocation: Boolean = true, // ✅ Added location fetching state
    val errorMessage: String? = null
)

@HiltViewModel
class RideSelectionViewModel @Inject constructor(
    private val apiService: GoogleMapsApiService,
    private val fusedLocationClient: FusedLocationProviderClient, // ✅ INJECT Location Client
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideSelectionState())
    val uiState = _uiState.asStateFlow()

    // ✅✅✅ START: THIS IS THE FIX ✅✅✅
    // Read the correct argument names defined in navgraph.kt
    private val dropPlaceId: String = savedStateHandle.get<String>("dropPlaceId") ?: ""
    private val encodedDropDescription: String = savedStateHandle.get<String>("dropDescription") ?: ""
    private val dropDescription: String = decodeUrlString(encodedDropDescription)

    private val pickupPlaceId: String? = savedStateHandle.get<String>("pickupPlaceId")
    private val encodedPickupDescription: String? = savedStateHandle.get<String>("pickupDescription")
    // ✅✅✅ END: THIS IS THE FIX ✅✅✅

    private val pickupDescription: String = decodeUrlString(encodedPickupDescription ?: "Your Current Location")

    private val isPickupCurrentLocation = pickupPlaceId == "current_location" || pickupPlaceId == null


    // ✅ MODIFIED: Ride options now include subtitle and placeholder duration
    private val initialRideOptions = listOf(
        RideOption(1, Icons.Default.TwoWheeler, "Bike", "2 mins away", 91, subtitle = "Quick Bike rides"),
        RideOption(2, Icons.Default.ElectricRickshaw, "Auto", "2 mins away", 148, subtitle = "Affordable Auto rides"),
        RideOption(3, Icons.Default.LocalTaxi, "Cab Economy", "2 mins away", 217, subtitle = "Comfy, economical"),
        RideOption(4, Icons.Default.Stars, "Cab Premium", "5 mins away", 274, subtitle = "Spacious & top-rated")
    )

    // StateFlow to hold ride options, allowing updates with duration
    private val _rideOptions = MutableStateFlow(initialRideOptions)
    val rideOptions = _rideOptions.asStateFlow()

    init {
        // ✅ MODIFIED: Set both descriptions from nav args
        _uiState.value = _uiState.value.copy(
            dropDescription = dropDescription,
            pickupDescription = pickupDescription
        )

        // ✅ MODIFIED: Decide which logic path to take
        if (isPickupCurrentLocation) {
            getCurrentLocationAndProceed()
        } else {
            // We have a specific pickup, fetch both locations
            fetchSpecificLocationsAndRoute()
        }
    }

    // ✅ ADDED: Helper to decode URL strings safely
    private fun decodeUrlString(encoded: String): String {
        return try {
            URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            encoded
        }
    }

    // --- Location Fetching ---

    @SuppressLint("MissingPermission") // IMPORTANT: Handle permissions properly!
    private fun getCurrentLocationAndProceed() {
        _uiState.value = _uiState.value.copy(isFetchingLocation = true)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                _uiState.value = _uiState.value.copy(isFetchingLocation = false)
                // Got location, now fetch drop details and route
                fetchRoute(currentLatLng, "place_id:$dropPlaceId")
            } else {
                requestNewLocation()
            }
        }.addOnFailureListener {
            requestNewLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(isFetchingLocation = false)
                    // Got location, now fetch drop details and route
                    fetchRoute(currentLatLng, "place_id:$dropPlaceId")
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not fetch current location.",
                        isFetchingLocation = false
                    )
                }
                fusedLocationClient.removeLocationUpdates(this)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Location services unavailable.",
                        isFetchingLocation = false
                    )
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    // --- API Calls ---

    // ✅ ADDED: New function to fetch specific pickup/drop coordinates
    private fun fetchSpecificLocationsAndRoute() {
        if (pickupPlaceId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Missing Pickup Location", isLoading = false, isFetchingLocation = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isFetchingLocation = false)
            try {
                // Fetch pickup and drop coordinates in parallel
                val pickupLatLngDeferred = async { fetchPlaceLatLng(pickupPlaceId) }
                val dropLatLngDeferred = async { fetchPlaceLatLng(dropPlaceId) }

                val pickupLatLng = pickupLatLngDeferred.await()
                val dropLatLng = dropLatLngDeferred.await()

                if (pickupLatLng != null && dropLatLng != null) {
                    // Both locations fetched, now get the route
                    fetchRoute(pickupLatLng, "place_id:$dropPlaceId")
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not get location details.",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "An unknown network error occurred",
                    isLoading = false
                )
            }
        }
    }

    // ✅ ADDED: Reusable function to fetch LatLng for any placeId
    private suspend fun fetchPlaceLatLng(placeId: String): LatLng? {
        return try {
            val response = apiService.getPlaceDetails(placeId)
            if (response.status == "OK" && response.result?.geometry?.location != null) {
                val loc = response.result.geometry.location
                LatLng(loc.lat, loc.lng)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ✅ MODIFIED: Renamed and generalized this function
    private fun fetchRoute(pickupLatLng: LatLng, destinationString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // We already have pickup LatLng, now fetch drop LatLng (for marker)
                // and directions all at once
                val dropLatLng = if (destinationString.startsWith("place_id:")) {
                    fetchPlaceLatLng(dropPlaceId)
                } else {
                    null // Handle other destination types if needed
                }

                _uiState.value = _uiState.value.copy(
                    pickupLocation = pickupLatLng,
                    dropLocation = dropLatLng
                )

                if (dropLatLng == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not get drop location details.",
                        isLoading = false
                    )
                    return@launch
                }

                // Check if locations are the same
                if (areLocationsTooClose(pickupLatLng, dropLatLng)) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Pickup and drop locations are the same.",
                        isLoading = false,
                        routePolyline = emptyList(),
                        tripDurationSeconds = 0
                    )
                    updateRideOptionsDuration(0)
                    return@launch
                }

                val originString = "${pickupLatLng.latitude},${pickupLatLng.longitude}"
                val directionsResponse = apiService.getDirections(
                    origin = originString,
                    destination = destinationString
                )

                if (directionsResponse.status == "OK" && directionsResponse.routes.isNotEmpty()) {
                    val route = directionsResponse.routes[0]
                    val points = route.overviewPolyline.points
                    val decodedPolyline = PolyUtil.decode(points)
                    val durationSeconds = route.legs.firstOrNull()?.duration?.value

                    _uiState.value = _uiState.value.copy(
                        routePolyline = decodedPolyline,
                        tripDurationSeconds = durationSeconds,
                        isLoading = false,
                        errorMessage = null
                    )
                    updateRideOptionsDuration(durationSeconds)
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not get directions: ${directionsResponse.status}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "An unknown network error occurred",
                    isLoading = false
                )
                e.printStackTrace()
            }
        }
    }

    // Helper to update ride options with fetched duration
    private fun updateRideOptionsDuration(durationSeconds: Int?) {
        val durationMinutes = durationSeconds?.let { (it / 60.0).roundToInt() }
        _rideOptions.update { currentOptions ->
            currentOptions.map { it.copy(estimatedDurationMinutes = durationMinutes) }
        }
    }

    // ADDED: Helper function to check distance
    private fun areLocationsTooClose(start: LatLng, end: LatLng, thresholdMeters: Float = 50f): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0] < thresholdMeters
    }

    // ADDED: Function to dismiss the error message from the UI
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}