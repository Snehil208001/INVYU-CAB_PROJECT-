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
// ✅ Import the updated models
import com.example.invyucab_project.domain.model.RideOption
import com.example.invyucab_project.domain.model.RideSelectionState
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
    private val fusedLocationClient: FusedLocationProviderClient,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RideSelectionState())
    val uiState = _uiState.asStateFlow()

    // --- Dropped PlaceId and Description ---
    private val dropPlaceId: String? = savedStateHandle.get<String>("dropPlaceId")
    private val encodedDropDescription: String? = savedStateHandle.get<String>("dropDescription")
    private val dropDescription: String = decodeUrlString(encodedDropDescription ?: "")

    // --- Pickup PlaceId and Description ---
    private val pickupPlaceId: String? = savedStateHandle.get<String>("pickupPlaceId")
    private val encodedPickupDescription: String? = savedStateHandle.get<String>("pickupDescription")
    private val pickupDescription: String = decodeUrlString(encodedPickupDescription ?: "Your Current Location")

    private val isPickupCurrentLocation = pickupPlaceId == "current_location" || pickupPlaceId == null

    // ✅✅✅ CONFLICT FIX: REMOVED the separate _rideOptions flow ✅✅✅
    // All state is now in _uiState

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
            initializeRideOptions() // This now updates the _uiState
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

    // ✅ CONFLICT FIX: This now updates the list INSIDE _uiState
    private fun initializeRideOptions() {
        val options = listOf(
            RideOption(1, Icons.Default.TwoWheeler, "Bike", "2 mins away", subtitle = "Quick Bike rides"),
            RideOption(2, Icons.Default.ElectricRickshaw, "Auto", "2 mins away", subtitle = "Affordable Auto rides"),
            RideOption(3, Icons.Default.LocalTaxi, "Cab Economy", "2 mins away", subtitle = "Comfy, economical"),
            RideOption(4, Icons.Default.Stars, "Cab Premium", "5 mins away", subtitle = "Spacious & top-rated")
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
                    pickupLocation = currentLatLng // Save pickup location
                )}
                onLocationsReady(currentLatLng) // Proceed
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

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    _uiState.update { it.copy(
                        isFetchingLocation = false,
                        pickupLocation = currentLatLng // Save pickup location
                    )}
                    onLocationsReady(currentLatLng) // Proceed
                } ?: run {
                    _uiState.update { it.copy(
                        errorMessage = "Could not fetch current location.",
                        isFetchingLocation = false
                    )}
                }
            }
        }
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
                                dropLocation = dropResult.data // Both locations are set here
                            )}
                            onLocationsReady(pickupResult.data) // Proceed
                        } else {
                            _uiState.update { it.copy(errorMessage = "Could not get location details.", isLoading = false) }
                        }
                    }
                    pickupResult is Resource.Error -> _uiState.update { it.copy(errorMessage = pickupResult.message, isLoading = false) }
                    dropResult is Resource.Error -> _uiState.update { it.copy(errorMessage = dropResult.message, isLoading = false) }
                }
            }.launchIn(this) // Use the 'this' scope from viewModelScope.launch
        }
    }

    // ✅✅✅ --- CRASH FIX IS HERE --- ✅✅✅
    private fun onLocationsReady(pickupLatLng: LatLng) {
        if (dropPlaceId == null) {
            _uiState.update { it.copy(errorMessage = "Drop-off location ID is missing.", isLoading = false) }
            return
        }

        // Get Route Info first
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

                    // CRASH FIX: Check if we have dropLocation LatLng.
                    // If not, fetch it BEFORE fetching prices.
                    if (_uiState.value.dropLocation == null) {
                        fetchDropLocationAndThenPrices(pickupLatLng)
                    } else {
                        // We have both LatLngs, safe to fetch prices
                        fetchAllRideData(pickupLatLng, _uiState.value.dropLocation!!, routeInfo.durationSeconds, routeInfo.distanceMeters)
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
            }
        }.launchIn(viewModelScope)
    }

    // This is the new helper function that fixes the crash
    private fun fetchDropLocationAndThenPrices(pickupLatLng: LatLng) {
        if (dropPlaceId == null) return // Should be impossible here

        viewModelScope.launch {
            getPlaceDetailsUseCase.invoke(dropPlaceId).collect { dropResult ->
                when (dropResult) {
                    is Resource.Success -> {
                        val dropLatLng = dropResult.data
                        if (dropLatLng != null) {
                            _uiState.update { it.copy(dropLocation = dropLatLng, isLoading = false) }
                            // NOW it's safe to call fetchAllRideData
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

    // ✅ CONFLICT FIX: This function now updates the _uiState.rideOptions
    private fun fetchAllRideData(pickup: LatLng, drop: LatLng, durationSeconds: Int?, distanceMeters: Int?) {
        val durationMinutes = durationSeconds?.let { (it / 60.0).roundToInt() }
        val distanceKm = (distanceMeters ?: 0) / 1000.0
        val distanceString = "%.1f km".format(distanceKm)

        // Set initial loading state for prices
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

        // Call UseCase
        getRidePricingUseCase.invoke(pickup, drop, _uiState.value.rideOptions).onEach { result ->
            when (result) {
                is Resource.Loading -> { /* Handled above */ }
                is Resource.Success -> {
                    // Success, update with prices
                    _uiState.update { it.copy(rideOptions = result.data!!) }
                }
                is Resource.Error -> {
                    // Error, update with N/A
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

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
        _apiError.value = null
    }
}