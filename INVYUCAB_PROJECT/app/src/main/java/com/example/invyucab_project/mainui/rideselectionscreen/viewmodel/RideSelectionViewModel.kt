package com.example.invyucab_project.mainui.rideselectionscreen.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.api.GoogleMapsApiService
import com.example.invyucab_project.data.models.GetPricingRequest
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

// ✅ MODIFIED RideOption: price is now added
data class RideOption(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val description: String, // ETA like "2 mins away"
    val price: String? = null, // ✅ RE-ADDED
    val subtitle: String? = null,
    val estimatedDurationMinutes: Int? = null, // Trip duration
    val estimatedDistanceKm: String? = null,
    val isLoadingPrice: Boolean = true // ✅ ADDED
)

data class RideSelectionState(
    val pickupLocation: LatLng? = null,
    val dropLocation: LatLng? = null,
    val pickupDescription: String = "Fetching current location...",
    val dropDescription: String = "",
    val routePolyline: List<LatLng> = emptyList(),
    val tripDurationSeconds: Int? = null,
    val tripDistanceMeters: Int? = null,
    val isLoading: Boolean = false,
    val isFetchingLocation: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class RideSelectionViewModel @Inject constructor(
    private val apiService: GoogleMapsApiService,
    private val customApiService: CustomApiService, // ✅ INJECTED
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideSelectionState())
    val uiState = _uiState.asStateFlow()

    private val dropPlaceId: String? = savedStateHandle.get<String>("dropPlaceId")
    private val encodedDropDescription: String? = savedStateHandle.get<String>("dropDescription")
    private val dropDescription: String = decodeUrlString(encodedDropDescription ?: "")

    private val pickupPlaceId: String? = savedStateHandle.get<String>("pickupPlaceId")
    private val encodedPickupDescription: String? = savedStateHandle.get<String>("pickupDescription")

    private val pickupDescription: String = decodeUrlString(encodedPickupDescription ?: "Your Current Location")

    private val isPickupCurrentLocation = pickupPlaceId == "current_location" || pickupPlaceId == null


    // ✅ MODIFIED: Price is null and isLoading is true by default from data class
    private val initialRideOptions = listOf(
        RideOption(1, Icons.Default.TwoWheeler, "Bike", "2 mins away", subtitle = "Quick Bike rides"),
        RideOption(2, Icons.Default.ElectricRickshaw, "Auto", "2 mins away", subtitle = "Affordable Auto rides"),
        RideOption(3, Icons.Default.LocalTaxi, "Cab Economy", "2 mins away", subtitle = "Comfy, economical"),
        RideOption(4, Icons.Default.Stars, "Cab Premium", "5 mins away", subtitle = "Spacious & top-rated")
    )

    private val _rideOptions = MutableStateFlow(initialRideOptions)
    val rideOptions = _rideOptions.asStateFlow()

    init {
        // ✅ ADDED: Guard clause to handle potential null navigation arguments
        if (dropPlaceId == null || encodedDropDescription == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Drop location is missing. Please go back.",
                isLoading = false,
                isFetchingLocation = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                dropDescription = dropDescription,
                pickupDescription = pickupDescription
            )

            if (isPickupCurrentLocation) {
                getCurrentLocationAndProceed()
            } else {
                fetchSpecificLocationsAndRoute()
            }
        }
    }

    private fun decodeUrlString(encoded: String): String {
        return try {
            URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            encoded
        }
    }

    // --- Location Fetching ---

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndProceed() {
        _uiState.value = _uiState.value.copy(isFetchingLocation = true)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                _uiState.value = _uiState.value.copy(isFetchingLocation = false)
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

    private fun fetchSpecificLocationsAndRoute() {
        if (pickupPlaceId == null || dropPlaceId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Missing Location", isLoading = false, isFetchingLocation = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isFetchingLocation = false)
            try {
                // ✅✅✅ START OF FIX ✅✅✅
                // Added !! because we already checked for null above
                val pickupLatLngDeferred = async { fetchPlaceLatLng(pickupPlaceId!!) }
                val dropLatLngDeferred = async { fetchPlaceLatLng(dropPlaceId!!) }
                // ✅✅✅ END OF FIX ✅✅✅

                val pickupLatLng = pickupLatLngDeferred.await()
                val dropLatLng = dropLatLngDeferred.await()

                if (pickupLatLng != null && dropLatLng != null) {
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

    private fun fetchRoute(pickupLatLng: LatLng, destinationString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // ✅✅✅ START OF FIX ✅✅✅
                // Added !! because we check for null in the init block
                val dropLatLng = if (destinationString.startsWith("place_id:")) {
                    fetchPlaceLatLng(dropPlaceId!!)
                } else {
                    null
                }
                // ✅✅✅ END OF FIX ✅✅✅

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

                if (areLocationsTooClose(pickupLatLng, dropLatLng)) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Pickup and drop locations are the same.",
                        isLoading = false,
                        routePolyline = emptyList(),
                        tripDurationSeconds = 0,
                        tripDistanceMeters = 0
                    )
                    fetchAllRideData(pickupLatLng, dropLatLng, 0, 0)
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
                    val leg = route.legs.firstOrNull()
                    val durationSeconds = leg?.duration?.value
                    val distanceMeters = leg?.distance?.value

                    _uiState.value = _uiState.value.copy(
                        routePolyline = decodedPolyline,
                        tripDurationSeconds = durationSeconds,
                        tripDistanceMeters = distanceMeters,
                        isLoading = false,
                        errorMessage = null
                    )
                    fetchAllRideData(pickupLatLng, dropLatLng, durationSeconds, distanceMeters)
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

    private fun fetchAllRideData(pickup: LatLng, drop: LatLng, durationSeconds: Int?, distanceMeters: Int?) {
        val durationMinutes = durationSeconds?.let { (it / 60.0).roundToInt() }
        val distanceKm = (distanceMeters ?: 0) / 1000.0
        val distanceString = "%.1f km".format(distanceKm)

        _rideOptions.update { currentOptions ->
            currentOptions.map { it.copy(isLoadingPrice = true) }
        }

        viewModelScope.launch {
            try {
                val request = GetPricingRequest(
                    pickupLat = pickup.latitude,
                    pickupLng = pickup.longitude,
                    dropLat = drop.latitude,
                    dropLng = drop.longitude
                )
                val response = customApiService.getPricing(request)

                // Check "success" field and "data" field
                if (response.success && response.data != null) {
                    _rideOptions.update { currentOptions ->
                        currentOptions.map { rideOption ->
                            val priceInfo = response.data.find {
                                it.vehicle_name?.equals(rideOption.name, ignoreCase = true) == true ||
                                        (it.vehicle_name?.equals("car", ignoreCase = true) == true && rideOption.name.contains("Cab"))
                            }

                            val formattedPrice = priceInfo?.let { "₹${it.total_price.roundToInt()}" } ?: "N/A"

                            rideOption.copy(
                                estimatedDurationMinutes = durationMinutes,
                                estimatedDistanceKm = distanceString,
                                price = formattedPrice,
                                isLoadingPrice = false
                            )
                        }
                    }
                } else {
                    throw Exception("API returned no price data")
                }
            } catch (e: Exception) {
                _rideOptions.update { currentOptions ->
                    currentOptions.map { rideOption ->
                        rideOption.copy(
                            estimatedDurationMinutes = durationMinutes,
                            estimatedDistanceKm = distanceString,
                            price = "N/A",
                            isLoadingPrice = false
                        )
                    }
                }
            }
        }
    }

    private fun areLocationsTooClose(start: LatLng, end: LatLng, thresholdMeters: Float = 50f): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0] < thresholdMeters
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}