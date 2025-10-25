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

    private val dropPlaceId: String = savedStateHandle.get<String>("placeId") ?: ""
    private val encodedDropDescription: String = savedStateHandle.get<String>("description") ?: ""
    private val dropDescription: String = try {
        URLDecoder.decode(encodedDropDescription, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        encodedDropDescription
    }

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
        _uiState.value = _uiState.value.copy(dropDescription = dropDescription)
        // Start fetching current location and then details/directions
        getCurrentLocationAndProceed()
    }

    // --- Location Fetching ---

    @SuppressLint("MissingPermission") // IMPORTANT: Handle permissions properly!
    private fun getCurrentLocationAndProceed() {
        _uiState.value = _uiState.value.copy(isFetchingLocation = true)

        // 1. Try getting last known location (quick)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                _uiState.value = _uiState.value.copy(
                    pickupLocation = currentLatLng,
                    pickupDescription = "Your Current Location", // Or reverse geocode later
                    isFetchingLocation = false
                )
                // Got location, now fetch drop details and route
                fetchDropLocationDetailsAndRoute(currentLatLng)
            } else {
                // No last location, request a fresh one
                requestNewLocation()
            }
        }.addOnFailureListener {
            // Handle error, maybe request new location
            requestNewLocation()
        }
    }

    @SuppressLint("MissingPermission") // IMPORTANT: Handle permissions properly!
    private fun requestNewLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // Interval 10s
            .setMinUpdateIntervalMillis(5000) // Fastest interval 5s
            .setMaxUpdates(1) // We only need one update here
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(
                        pickupLocation = currentLatLng,
                        pickupDescription = "Your Current Location",
                        isFetchingLocation = false
                    )
                    // Got location, now fetch drop details and route
                    fetchDropLocationDetailsAndRoute(currentLatLng)
                } ?: run {
                    // Still couldn't get location
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not fetch current location.",
                        isFetchingLocation = false
                    )
                }
                // Stop listening after getting the location
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

        // Start listening
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    // --- API Calls ---

    private fun fetchDropLocationDetailsAndRoute(pickupLatLng: LatLng) {
        if (dropPlaceId.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No drop location selected")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Fetch Drop Location Coordinates
                val detailsResponse = apiService.getPlaceDetails(dropPlaceId)
                val dropLatLng = if (detailsResponse.status == "OK" && detailsResponse.result?.geometry?.location != null) {
                    val loc = detailsResponse.result.geometry.location
                    LatLng(loc.lat, loc.lng)
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not get drop location details: ${detailsResponse.status}",
                        isLoading = false
                    )
                    null // Indicate failure
                }

                // Update state with drop location immediately if successful
                dropLatLng?.let {
                    _uiState.value = _uiState.value.copy(dropLocation = it)
                }

                // 2. Fetch Directions (Only if dropLatLng was found)
                if (dropLatLng != null) {

                    // ✅✅✅ START FIX: Check if locations are the same
                    if (areLocationsTooClose(pickupLatLng, dropLatLng)) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Pickup and drop locations are the same.",
                            isLoading = false,
                            routePolyline = emptyList(), // No route
                            tripDurationSeconds = 0
                        )
                        updateRideOptionsDuration(0) // Update rides with 0 min duration
                        return@launch // Stop further execution
                    }
                    // ✅✅✅ END FIX

                    // Use LatLng for origin, place_id for destination
                    val originString = "${pickupLatLng.latitude},${pickupLatLng.longitude}"
                    val destinationString = "place_id:$dropPlaceId"

                    val directionsResponse = apiService.getDirections(
                        origin = originString,
                        destination = destinationString
                    )

                    if (directionsResponse.status == "OK" && directionsResponse.routes.isNotEmpty()) {
                        val route = directionsResponse.routes[0]
                        val points = route.overviewPolyline.points
                        val decodedPolyline = PolyUtil.decode(points)

                        // ✅ Get duration from the first leg (usually only one leg for simple routes)
                        val durationSeconds = route.legs.firstOrNull()?.duration?.value

                        _uiState.value = _uiState.value.copy(
                            routePolyline = decodedPolyline,
                            tripDurationSeconds = durationSeconds, // Store duration
                            isLoading = false,
                            errorMessage = null // Clear previous errors
                        )
                        // ✅ Update ride options with duration
                        updateRideOptionsDuration(durationSeconds)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Could not get directions: ${directionsResponse.status}",
                            isLoading = false
                        )
                    }
                }
                // If dropLatLng was null, isLoading was already set to false

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "An unknown network error occurred",
                    isLoading = false
                )
                e.printStackTrace() // Log the error
            }
        }
    }

    // ✅ Helper to update ride options with fetched duration
    private fun updateRideOptionsDuration(durationSeconds: Int?) {
        val durationMinutes = durationSeconds?.let { (it / 60.0).roundToInt() }
        _rideOptions.update { currentOptions ->
            currentOptions.map { it.copy(estimatedDurationMinutes = durationMinutes) }
        }
    }

    // ✅ ADDED: Helper function to check distance
    private fun areLocationsTooClose(start: LatLng, end: LatLng, thresholdMeters: Float = 50f): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0] < thresholdMeters // Returns true if distance is less than 50 meters
    }

    // ✅ ADDED: Function to dismiss the error message from the UI
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}