package com.example.invyucab_project.mainui.rideselectionscreen.viewmodel

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.CreateRideRequest
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.model.RideOption
import com.example.invyucab_project.domain.model.RideSelectionState
import com.example.invyucab_project.domain.usecase.CreateRideUseCase
import com.example.invyucab_project.domain.usecase.GetDirectionsAndRouteUseCase
import com.example.invyucab_project.domain.usecase.GetPlaceDetailsUseCase
import com.example.invyucab_project.domain.usecase.GetRidePricingUseCase
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class RideSelectionViewModel @Inject constructor(
    private val getPlaceDetailsUseCase: GetPlaceDetailsUseCase,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase,
    private val getRidePricingUseCase: GetRidePricingUseCase,
    private val createRideUseCase: CreateRideUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RideSelectionState())
    val uiState = _uiState.asStateFlow()

    private val _bookingState = MutableStateFlow(BookingState())
    val bookingState = _bookingState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RideNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // --- Place IDs & Descriptions ---
    private val dropPlaceId: String? = savedStateHandle.get<String>("dropPlaceId")
    private val encodedDropDescription: String? = savedStateHandle.get<String>("dropDescription")
    private val dropDescription: String = decodeUrlString(encodedDropDescription ?: "")

    private val pickupPlaceId: String? = savedStateHandle.get<String>("pickupPlaceId")
    private val encodedPickupDescription: String? = savedStateHandle.get<String>("pickupDescription")
    private val pickupDescription: String = decodeUrlString(encodedPickupDescription ?: "Your Current Location")

    // --- LatLng Params (for Recent Rides) ---
    private val pickupLat: Double = (savedStateHandle.get<Float>("pickupLat") ?: 0f).toDouble()
    private val pickupLng: Double = (savedStateHandle.get<Float>("pickupLng") ?: 0f).toDouble()
    private val dropLat: Double = (savedStateHandle.get<Float>("dropLat") ?: 0f).toDouble()
    private val dropLng: Double = (savedStateHandle.get<Float>("dropLng") ?: 0f).toDouble()

    private val isPickupCurrentLocation = pickupPlaceId == "current_location" || pickupPlaceId == null

    init {
        // 1. Populate coordinates from arguments
        val hasValidDropCoords = dropLat != 0.0 && dropLng != 0.0
        val hasValidPickupCoords = pickupLat != 0.0 && pickupLng != 0.0

        if (hasValidDropCoords) {
            _uiState.update { it.copy(dropLocation = LatLng(dropLat, dropLng)) }
        }
        if (hasValidPickupCoords) {
            _uiState.update { it.copy(pickupLocation = LatLng(pickupLat, pickupLng)) }
        }

        // 2. Initialize UI text
        _uiState.update { it.copy(
            dropDescription = if (dropDescription.isBlank()) "Drop Location" else dropDescription,
            pickupDescription = pickupDescription
        )}
        initializeRideOptions()

        // 3. Decide Flow (Fix for Infinite Loading)
        val currentPickup = _uiState.value.pickupLocation
        val currentDrop = _uiState.value.dropLocation

        if (currentPickup != null && currentDrop != null) {
            // ✅ FIX: Explicitly stop fetching location since we already have it
            _uiState.update { it.copy(isFetchingLocation = false) }
            onLocationsReady(currentPickup)
        } else if (isPickupCurrentLocation) {
            getCurrentLocationAndProceed()
        } else {
            fetchSpecificLocationsAndRoute()
        }
    }

    private fun decodeUrlString(encoded: String): String {
        return try {
            URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            encoded
        }
    }

    private fun initializeRideOptions() {
        val options = listOf(
            RideOption(1, Icons.Default.TwoWheeler, "Bike", "", null),
            RideOption(2, Icons.Default.ElectricRickshaw, "Auto", "", null),
            RideOption(3, Icons.Default.LocalTaxi, "Car", "", null)
        )
        _uiState.update { it.copy(rideOptions = options) }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndProceed() {
        _uiState.update { it.copy(isFetchingLocation = true) }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                _uiState.update { it.copy(isFetchingLocation = false, pickupLocation = currentLatLng) }
                onLocationsReady(currentLatLng)
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
        val locationCallback = MyLocationCallback()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun fetchSpecificLocationsAndRoute() {
        if (pickupPlaceId.isNullOrEmpty() || dropPlaceId.isNullOrEmpty()) {
            if (_uiState.value.pickupLocation != null && _uiState.value.dropLocation != null) {
                _uiState.update { it.copy(isFetchingLocation = false) }
                return
            }
            _uiState.update { it.copy(errorMessage = "Missing Location Data", isLoading = false, isFetchingLocation = false) }
            return
        }

        val pickupFlow = getPlaceDetailsUseCase.invoke(pickupPlaceId)
        val dropFlow = getPlaceDetailsUseCase.invoke(dropPlaceId)

        viewModelScope.launch {
            pickupFlow.zip(dropFlow) { p, d -> Pair(p, d) }
                .onEach { (pickupResult, dropResult) ->
                    if (pickupResult is Resource.Success && dropResult is Resource.Success) {
                        if (pickupResult.data != null && dropResult.data != null) {
                            _uiState.update { it.copy(pickupLocation = pickupResult.data, dropLocation = dropResult.data, isFetchingLocation = false) }
                            onLocationsReady(pickupResult.data)
                        }
                    } else if (pickupResult is Resource.Error) {
                        _uiState.update { it.copy(errorMessage = pickupResult.message, isLoading = false, isFetchingLocation = false) }
                    }
                }.launchIn(this)
        }
    }

    private fun onLocationsReady(pickupLatLng: LatLng) {
        val dropLocation = _uiState.value.dropLocation

        if (dropLocation == null) {
            if (!dropPlaceId.isNullOrEmpty()) {
                fetchDropLocationAndThenPrices(pickupLatLng)
            } else {
                _uiState.update { it.copy(errorMessage = "Drop location missing", isLoading = false) }
            }
            return
        }

        // ✅ FIX: Correctly construct destination for API
        val destinationParam = if (!dropPlaceId.isNullOrBlank()) {
            "place_id:$dropPlaceId"
        } else {
            "${dropLocation.latitude},${dropLocation.longitude}"
        }

        Log.d("RideSelectionViewModel", "Fetching route -> Origin: $pickupLatLng, Destination: $destinationParam")

        getDirectionsAndRouteUseCase.invoke(pickupLatLng, destinationParam).onEach { result ->
            when (result) {
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                is Resource.Success -> {
                    val routeInfo = result.data!!
                    _uiState.update { it.copy(
                        routePolyline = routeInfo.polyline,
                        tripDurationSeconds = routeInfo.durationSeconds,
                        tripDistanceMeters = routeInfo.distanceMeters,
                        isLoading = false,
                        errorMessage = null
                    )}
                    fetchAllRideData(pickupLatLng, dropLocation, routeInfo.durationSeconds, routeInfo.distanceMeters)
                }
                is Resource.Error -> {
                    // Even if route fails, stop loading
                    _uiState.update { it.copy(errorMessage = "Route Error: ${result.message}", isLoading = false) }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchDropLocationAndThenPrices(pickupLatLng: LatLng) {
        if (dropPlaceId == null) return
        viewModelScope.launch {
            getPlaceDetailsUseCase.invoke(dropPlaceId).collect { dropResult ->
                if (dropResult is Resource.Success) {
                    val dropLatLng = dropResult.data
                    if (dropLatLng != null) {
                        _uiState.update { it.copy(dropLocation = dropLatLng, isLoading = false) }
                        onLocationsReady(pickupLatLng)
                    }
                }
            }
        }
    }

    private fun fetchAllRideData(pickup: LatLng, drop: LatLng, durationSeconds: Int?, distanceMeters: Int?) {
        val durationMinutes = durationSeconds?.let { (it / 60.0).roundToInt() }
        val distanceKm = (distanceMeters ?: 0) / 1000.0
        val distanceString = "%.1f km".format(distanceKm)

        _uiState.update { currentState ->
            val updatedOptions = currentState.rideOptions.map {
                it.copy(isLoadingPrice = true, estimatedDurationMinutes = durationMinutes, estimatedDistanceKm = distanceString)
            }
            currentState.copy(rideOptions = updatedOptions)
        }

        getRidePricingUseCase.invoke(pickup, drop, _uiState.value.rideOptions).onEach { result ->
            if (result is Resource.Success) {
                _uiState.update { currentState ->
                    val newOptions = result.data!!
                    val updatedOptions = currentState.rideOptions.map { current ->
                        val new = newOptions.find { it.name.equals(current.name, ignoreCase = true) }
                        current.copy(price = new?.price ?: current.price, isLoadingPrice = false)
                    }
                    currentState.copy(rideOptions = updatedOptions)
                }
            } else {
                // Stop price loading on error
                _uiState.update { currentState ->
                    val updatedOptions = currentState.rideOptions.map { it.copy(isLoadingPrice = false) }
                    currentState.copy(rideOptions = updatedOptions)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onBookRideClicked(selectedRideId: Int) {
        val currentState = _uiState.value
        val selectedRide = currentState.rideOptions.find { it.id == selectedRideId }
        val pickup = currentState.pickupLocation
        val drop = currentState.dropLocation
        val userIdString = userPreferencesRepository.getUserId()
        val userId = userIdString?.toIntOrNull()

        // 👇 LOG EVERYTHING TO SEE WHAT IS NULL
        Log.d("RideBookingDebug", "Attempting to book...")
        Log.d("RideBookingDebug", "User ID: $userId")
        Log.d("RideBookingDebug", "Pickup: $pickup")
        Log.d("RideBookingDebug", "Drop: $drop")
        Log.d("RideBookingDebug", "Selected Ride: ${selectedRide?.name}")
        Log.d("RideBookingDebug", "Price: ${selectedRide?.price}")

        if (userId == null) {
            Log.e("RideBookingDebug", "ERROR: User ID is null. Session expired.")
            userPreferencesRepository.clearUserStatus()
            _uiState.update { it.copy(errorMessage = "Session expired. Please restart app.") }
            return
        }

        if (selectedRide == null || pickup == null || drop == null || selectedRide.price == null) {
            Log.e("RideBookingDebug", "ERROR: Missing details. Price is probably null.")
            _uiState.update { it.copy(errorMessage = "Cannot book ride, missing details.") }
            return
        }

        val priceValue = selectedRide.price.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0

        val request = CreateRideRequest(
            riderId = userId,
            pickupLatitude = pickup.latitude,
            pickupLongitude = pickup.longitude,
            dropLatitude = drop.latitude,
            dropLongitude = drop.longitude,
            estimatedPrice = priceValue,
            status = "requested"
        )

        createRideUseCase(request).onEach { result ->
            when (result) {
                is Resource.Loading -> _bookingState.update { it.copy(isLoading = true) }
                is Resource.Success -> {
                    Log.d("RideBookingDebug", "Booking Success!")
                    _bookingState.update { it.copy(isLoading = false) }
                    val rawData = result.data?.data
                    var rideId: Int? = null
                    var userPin: Int = 1234

                    if (rawData is Double) rideId = rawData.toInt()
                    else if (rawData is Int) rideId = rawData
                    else if (rawData is Map<*, *>) {
                        rideId = (rawData["ride_id"] as? Double)?.toInt()
                        userPin = (rawData["user_pin"] as? Double)?.toInt() ?: 1234
                    }

                    if (rideId != null) {
                        _navigationEvent.emit(RideNavigationEvent.NavigateToBooking(
                            rideId = rideId,
                            userPin = userPin,
                            pickup = pickup,
                            drop = drop,
                            pickupAddress = currentState.pickupDescription,
                            dropAddress = currentState.dropDescription,
                            dropPlaceId = dropPlaceId ?: ""
                        ))
                    } else {
                        Log.e("RideBookingDebug", "Booking failed: Invalid server response data: $rawData")
                        _uiState.update { it.copy(errorMessage = "Booking failed: Invalid server response") }
                    }
                }
                is Resource.Error -> {
                    Log.e("RideBookingDebug", "Booking API Failed: ${result.message}")
                    _bookingState.update { it.copy(isLoading = false) }
                    _uiState.update { it.copy(errorMessage = result.message ?: "Booking failed") }
                }
            }
        }.launchIn(viewModelScope)
    }

    private inner class MyLocationCallback : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            fusedLocationClient.removeLocationUpdates(this)
            locationResult.lastLocation?.let { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                _uiState.update { it.copy(isFetchingLocation = false, pickupLocation = currentLatLng)}
                onLocationsReady(currentLatLng)
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class BookingState(val isLoading: Boolean = false)

sealed class RideNavigationEvent {
    data class NavigateToBooking(
        val rideId: Int,
        val userPin: Int,
        val pickup: LatLng,
        val drop: LatLng,
        val pickupAddress: String,
        val dropAddress: String,
        val dropPlaceId: String
    ) : RideNavigationEvent()
}