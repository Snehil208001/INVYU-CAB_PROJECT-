package com.example.invyucab_project.mainui.rideselectionscreen.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.CreateRideRequest
import com.example.invyucab_project.data.models.CreateRideResponse
import com.example.invyucab_project.data.preferences.UserPreferencesRepository // ✅ ADDED IMPORT
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
    private val userPreferencesRepository: UserPreferencesRepository, // ✅ INJECTED REPOSITORY
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RideSelectionState())
    val uiState = _uiState.asStateFlow()

    // --- Booking Loading State ---
    private val _bookingState = MutableStateFlow(BookingState())
    val bookingState = _bookingState.asStateFlow()

    // --- Navigation Event ---
    private val _navigationEvent = MutableSharedFlow<RideNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // --- Dropped PlaceId and Description ---
    private val dropPlaceId: String? = savedStateHandle.get<String>("dropPlaceId")
    private val encodedDropDescription: String? = savedStateHandle.get<String>("dropDescription")
    private val dropDescription: String = decodeUrlString(encodedDropDescription ?: "")

    // --- Pickup PlaceId and Description ---
    private val pickupPlaceId: String? = savedStateHandle.get<String>("pickupPlaceId")
    private val encodedPickupDescription: String? = savedStateHandle.get<String>("pickupDescription")
    private val pickupDescription: String = decodeUrlString(encodedPickupDescription ?: "Your Current Location")

    private val isPickupCurrentLocation = pickupPlaceId == "current_location" || pickupPlaceId == null

    init {
        if (dropPlaceId == null || encodedDropDescription == null) {
            _uiState.update { it.copy(
                errorMessage = "Drop location is missing. Please go back.",
                isLoading = false,
                isFetchingLocation = false
            )}
        } else {
            _uiState.update { it.copy(
                dropDescription = dropDescription,
                pickupDescription = pickupDescription
            )}
            initializeRideOptions()
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

    private fun initializeRideOptions() {
        val options = listOf(
            RideOption(
                id = 1,
                icon = Icons.Default.TwoWheeler,
                name = "Bike",
                description = "",
                subtitle = null
            ),
            RideOption(
                id = 2,
                icon = Icons.Default.ElectricRickshaw,
                name = "Auto",
                description = "",
                subtitle = null
            ),
            RideOption(
                id = 3,
                icon = Icons.Default.LocalTaxi,
                name = "Car",
                description = "",
                subtitle = null
            )
        )
        _uiState.update { it.copy(rideOptions = options) }
    }

    // --- Location Fetching ---
    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndProceed() {
        _uiState.update { it.copy(isFetchingLocation = true) }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                _uiState.update { it.copy(
                    isFetchingLocation = false,
                    pickupLocation = currentLatLng
                )}
                onLocationsReady(currentLatLng)
            } else {
                requestNewLocation()
            }
        }.addOnFailureListener { requestNewLocation() }
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

    // --- API Calls ---
    private fun fetchSpecificLocationsAndRoute() {
        if (pickupPlaceId == null || dropPlaceId == null) {
            _uiState.update { it.copy(errorMessage = "Missing Location", isLoading = false, isFetchingLocation = false) }
            return
        }

        val pickupFlow = getPlaceDetailsUseCase.invoke(pickupPlaceId)
        val dropFlow = getPlaceDetailsUseCase.invoke(dropPlaceId)

        viewModelScope.launch {
            pickupFlow.zip(dropFlow) { pickupResult, dropResult ->
                Pair(pickupResult, dropResult)
            }.onEach { (pickupResult, dropResult) ->
                when {
                    pickupResult is Resource.Loading || dropResult is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, isFetchingLocation = false) }
                    }
                    pickupResult is Resource.Success && dropResult is Resource.Success -> {
                        if (pickupResult.data != null && dropResult.data != null) {
                            _uiState.update { it.copy(
                                pickupLocation = pickupResult.data,
                                dropLocation = dropResult.data
                            )}
                            onLocationsReady(pickupResult.data)
                        } else {
                            _uiState.update { it.copy(errorMessage = "Could not get location details.", isLoading = false) }
                        }
                    }
                    pickupResult is Resource.Error -> _uiState.update { it.copy(errorMessage = pickupResult.message, isLoading = false) }
                    dropResult is Resource.Error -> _uiState.update { it.copy(errorMessage = dropResult.message, isLoading = false) }
                }
            }.launchIn(this)
        }
    }

    private fun onLocationsReady(pickupLatLng: LatLng) {
        if (dropPlaceId == null) {
            _uiState.update { it.copy(errorMessage = "Drop-off location ID is missing.", isLoading = false) }
            return
        }

        getDirectionsAndRouteUseCase.invoke(pickupLatLng, dropPlaceId).onEach { result ->
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

                    if (_uiState.value.dropLocation == null) {
                        fetchDropLocationAndThenPrices(pickupLatLng)
                    } else {
                        fetchAllRideData(pickupLatLng, _uiState.value.dropLocation!!, routeInfo.durationSeconds, routeInfo.distanceMeters)
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchDropLocationAndThenPrices(pickupLatLng: LatLng) {
        if (dropPlaceId == null) return

        viewModelScope.launch {
            getPlaceDetailsUseCase.invoke(dropPlaceId).collect { dropResult ->
                when (dropResult) {
                    is Resource.Success -> {
                        val dropLatLng = dropResult.data
                        if (dropLatLng != null) {
                            _uiState.update { it.copy(dropLocation = dropLatLng, isLoading = false) }
                            fetchAllRideData(
                                pickup = pickupLatLng,
                                drop = dropLatLng,
                                durationSeconds = _uiState.value.tripDurationSeconds,
                                distanceMeters = _uiState.value.tripDistanceMeters
                            )
                        } else {
                            _uiState.update { it.copy(errorMessage = "Could not get drop-off location details.", isLoading = false) }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = dropResult.message, isLoading = false) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
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
                it.copy(
                    isLoadingPrice = true,
                    estimatedDurationMinutes = durationMinutes,
                    estimatedDistanceKm = distanceString
                )
            }
            currentState.copy(rideOptions = updatedOptions)
        }

        getRidePricingUseCase.invoke(pickup, drop, _uiState.value.rideOptions).onEach { result ->
            when (result) {
                is Resource.Loading -> { /* Handled above */ }
                is Resource.Success -> {
                    _uiState.update { currentState ->
                        val newOptions = result.data!!
                        val currentOptions = currentState.rideOptions

                        val updatedOptions = currentOptions.map { current ->
                            val new = newOptions.find { it.name.equals(current.name, ignoreCase = true) }
                            current.copy(
                                price = new?.price ?: current.price,
                                isLoadingPrice = new?.isLoadingPrice ?: false
                            )
                        }
                        currentState.copy(rideOptions = updatedOptions)
                    }
                }
                is Resource.Error -> {
                    _apiError.value = result.message
                    _uiState.update { currentState ->
                        val updatedOptions = currentState.rideOptions.map {
                            it.copy(
                                price = "N/A",
                                isLoadingPrice = false
                            )
                        }
                        currentState.copy(rideOptions = updatedOptions)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    // --- START OF FIXED CODE ---
    fun onBookRideClicked(selectedRideId: Int) {
        val currentState = _uiState.value
        val selectedRide = currentState.rideOptions.find { it.id == selectedRideId }
        val pickup = currentState.pickupLocation
        val drop = currentState.dropLocation

        // ✅ 1. Get the actual User ID from preferences
        val userIdString = userPreferencesRepository.getUserId()
        val userId = userIdString?.toIntOrNull()

        // Check if User ID exists
        if (userId == null) {
            _uiState.update {
                it.copy(errorMessage = "User not identified. Please log in again.")
            }
            return
        }

        // Ensure we have all the data we need
        if (selectedRide == null || pickup == null || drop == null || selectedRide.price == null) {
            _uiState.update {
                it.copy(errorMessage = "Cannot book ride, missing details.")
            }
            return
        }

        // ✅ 2. Safe price parsing (removes '₹' or 'Rs.' etc)
        val priceValue = selectedRide.price.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0

        // Construct the request with the REAL riderId
        val request = CreateRideRequest(
            riderId = userId, // ✅ Using real user ID
            pickupLatitude = pickup.latitude,
            pickupLongitude = pickup.longitude,
            dropLatitude = drop.latitude,
            dropLongitude = drop.longitude,
            estimatedPrice = priceValue,
            status = "requested"
        )

        createRideUseCase(request).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _bookingState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    _bookingState.update { it.copy(isLoading = false) }
                    // Send event to UI to navigate
                    result.data?.let {
                        _navigationEvent.emit(RideNavigationEvent.NavigateToBooking(it.rideId))
                    }
                }
                is Resource.Error -> {
                    _bookingState.update { it.copy(isLoading = false) }
                    _uiState.update {
                        it.copy(errorMessage = result.message ?: "Booking failed")
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
    // --- END OF FIXED CODE ---


    /**
     * This inner class is used to fix a crash with Android Studio's Live Edit feature.
     */
    private inner class MyLocationCallback : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // We only need one update, so remove the callback immediately
            fusedLocationClient.removeLocationUpdates(this)

            locationResult.lastLocation?.let { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                _uiState.update { it.copy(
                    isFetchingLocation = false,
                    pickupLocation = currentLatLng
                )}
                // Now that we have the location, proceed to fetch the route
                onLocationsReady(currentLatLng)
            } ?: run {
                // This block runs if lastLocation is somehow null
                _uiState.update { it.copy(
                    errorMessage = "Could not fetch current location.",
                    isFetchingLocation = false
                )}
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
        _apiError.value = null
    }
}

data class BookingState(
    val isLoading: Boolean = false
)

sealed class RideNavigationEvent {
    data class NavigateToBooking(val rideId: Int) : RideNavigationEvent()
}