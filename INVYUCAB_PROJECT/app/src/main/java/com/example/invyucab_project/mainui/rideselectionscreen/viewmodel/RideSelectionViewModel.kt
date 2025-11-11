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
// import java.text.SimpleDateFormat // ❌ REMOVED
// import java.util.* // ❌ REMOVED
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

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    _uiState.update { it.copy(
                        isFetchingLocation = false,
                        pickupLocation = currentLatLng
                    )}
                    onLocationsReady(currentLatLng)
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
                    // ❌ REMOVED description = "Est. drop $dropTime"
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

    // ❌❌❌ REMOVED unused property ❌❌❌
    /*
    private val dropTime: String
        get() {
            val duration = _uiState.value.tripDurationSeconds ?: 0
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.SECOND, duration)
            val format = SimpleDateFormat("h:mm a", Locale.getDefault())
            return format.format(calendar.time).lowercase()
        }
    */

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
        _apiError.value = null
    }
}