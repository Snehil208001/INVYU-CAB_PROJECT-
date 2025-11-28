package com.example.invyucab_project.mainui.homescreen.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.model.AutocompletePrediction
import com.example.invyucab_project.domain.model.HomeUiState
import com.example.invyucab_project.domain.model.RecentRide
import com.example.invyucab_project.domain.model.SearchField
import com.example.invyucab_project.domain.usecase.GetAutocompletePredictionsUseCase
import com.example.invyucab_project.domain.usecase.GetRideHistoryUseCase
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val getAutocompletePredictionsUseCase: GetAutocompletePredictionsUseCase,
    private val locationManager: LocationManager,
    private val getRideHistoryUseCase: GetRideHistoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val application: Application
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        getCurrentLocation()
        fetchRecentRides()
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (!isLocationEnabled()) {
            _apiError.value = "Please turn on location services (GPS)."
            _uiState.update { it.copy(isFetchingLocation = false) }
            return
        }

        _uiState.update { it.copy(isFetchingLocation = true) }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                _uiState.update {
                    it.copy(
                        currentLocation = LatLng(location.latitude, location.longitude),
                        isFetchingLocation = false
                    )
                }
            } else {
                requestNewLocation()
            }
        }.addOnFailureListener {
            _apiError.value = "Failed to get location. Please enable GPS."
            _uiState.update { it.copy(isFetchingLocation = false) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        if (!isLocationEnabled()) {
            _apiError.value = "Please turn on location services (GPS)."
            _uiState.update { it.copy(isFetchingLocation = false) }
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                locationResult.lastLocation?.let { location ->
                    _uiState.update {
                        it.copy(
                            currentLocation = LatLng(location.latitude, location.longitude),
                            isFetchingLocation = false
                        )
                    }
                } ?: run {
                    _apiError.value = "Could not fetch current location."
                    _uiState.update { it.copy(isFetchingLocation = false) }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun fetchRecentRides() {
        val userIdStr = userPreferencesRepository.getUserId()
        val userId = userIdStr?.toIntOrNull() ?: return

        viewModelScope.launch {
            try {
                val response = getRideHistoryUseCase(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val rides = response.body()?.data ?: emptyList()

                    val recentItems = rides.reversed().take(3).map { item ->
                        val pickupLat = item.pickupLatitude?.toDoubleOrNull() ?: 0.0
                        val pickupLng = item.pickupLongitude?.toDoubleOrNull() ?: 0.0
                        val dropLat = item.dropLatitude?.toDoubleOrNull() ?: 0.0
                        val dropLng = item.dropLongitude?.toDoubleOrNull() ?: 0.0

                        val pickup = getAddressFromLatLng(pickupLat, pickupLng)
                        val drop = getAddressFromLatLng(dropLat, dropLng)

                        RecentRide(
                            rideId = item.rideId,
                            pickupAddress = pickup,
                            dropAddress = drop,
                            pickupLat = pickupLat,
                            pickupLng = pickupLng,
                            dropLat = dropLat,
                            dropLng = dropLng
                        )
                    }

                    _uiState.update { it.copy(recentRides = recentItems) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getAddressFromLatLng(lat: Double, lng: Double): String {
        if (lat == 0.0 || lng == 0.0) return "Location N/A"
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(application, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) addresses[0].getAddressLine(0) ?: "Unknown Location" else "Unknown Location"
            } catch (e: Exception) { "Unknown Location" }
        }
    }

    // ✅ ADDED: Handle click on recent ride
    fun onRecentRideClicked(ride: RecentRide) {
        viewModelScope.launch {
            sendEvent(
                UiEvent.Navigate(
                    Screen.RideSelectionScreen.createRoute(
                        dropPlaceId = "", // ✅ Fixed: Empty string prevents API call for "recent_ride"
                        dropDescription = ride.dropAddress,
                        pickupPlaceId = "", // ✅ Fixed: Empty string prevents API call for "recent_ride"
                        pickupDescription = ride.pickupAddress,
                        pickupLat = ride.pickupLat,
                        pickupLng = ride.pickupLng,
                        dropLat = ride.dropLat,
                        dropLng = ride.dropLng
                    )
                )
            )
        }
    }

    // --- Search Logic (Unchanged) ---

    fun onPickupQueryChange(query: String) {
        _uiState.update { it.copy(pickupQuery = query, activeField = SearchField.PICKUP) }
        if (query.isBlank()) {
            _uiState.update { it.copy(pickupResults = emptyList(), pickupPlaceId = null) }
            return
        }
        search(query, SearchField.PICKUP)
    }

    fun onDropQueryChange(query: String) {
        _uiState.update { it.copy(dropQuery = query, activeField = SearchField.DROP) }
        if (query.isBlank()) {
            _uiState.update { it.copy(dropResults = emptyList(), dropPlaceId = null) }
            return
        }
        search(query, SearchField.DROP)
    }

    fun onClearPickup() {
        _uiState.update { it.copy(pickupQuery = "", pickupPlaceId = null, pickupResults = emptyList(), activeField = SearchField.PICKUP) }
    }

    fun onClearDrop() {
        _uiState.update { it.copy(dropQuery = "", dropPlaceId = null, dropResults = emptyList()) }
    }

    fun onFocusChange(field: SearchField) {
        if (field == SearchField.PICKUP && _uiState.value.pickupQuery == "Your Current Location") {
            _uiState.update { it.copy(activeField = field, pickupQuery = "") }
        } else {
            _uiState.update { it.copy(activeField = field) }
        }
    }

    fun onFocusLost(field: SearchField) {
        if (field == SearchField.PICKUP) {
            if (_uiState.value.pickupQuery.isBlank()) {
                _uiState.update { it.copy(pickupQuery = "Your Current Location", pickupPlaceId = "current_location") }
            }
        }
    }

    private fun search(query: String, field: SearchField) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            val location = _uiState.value.currentLocation
            val locationString = location?.let { "${it.latitude},${it.longitude}" } ?: ""

            getAutocompletePredictionsUseCase(query, locationString)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> _uiState.update { it.copy(isSearching = true) }
                        is Resource.Error -> {
                            _apiError.value = result.message
                            _uiState.update { it.copy(isSearching = false) }
                        }
                        is Resource.Success -> {
                            val mappedPredictions = result.data?.map { prediction ->
                                AutocompletePrediction(
                                    placeId = prediction.placeId,
                                    primaryText = prediction.structuredFormatting.mainText,
                                    secondaryText = prediction.structuredFormatting.secondaryText ?: "",
                                    description = prediction.description
                                )
                            } ?: emptyList()

                            if (field == SearchField.PICKUP) {
                                _uiState.update { it.copy(pickupResults = mappedPredictions, isSearching = false) }
                            } else {
                                _uiState.update { it.copy(dropResults = mappedPredictions, isSearching = false) }
                            }
                        }
                    }
                }.launchIn(viewModelScope)
        }
    }

    fun onPredictionTapped(prediction: AutocompletePrediction) {
        if (_uiState.value.activeField == SearchField.PICKUP) {
            _uiState.update {
                it.copy(
                    pickupQuery = prediction.primaryText,
                    pickupPlaceId = prediction.placeId,
                    pickupResults = emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    dropQuery = prediction.primaryText,
                    dropPlaceId = prediction.placeId,
                    dropResults = emptyList()
                )
            }
        }
    }

    fun onContinueClicked() {
        val currentState = _uiState.value
        if (!currentState.pickupPlaceId.isNullOrBlank() && !currentState.dropPlaceId.isNullOrBlank()) {
            val pickupDesc = if (currentState.pickupPlaceId == "current_location") "Your Current Location" else currentState.pickupQuery

            viewModelScope.launch {
                sendEvent(
                    UiEvent.Navigate(
                        Screen.RideSelectionScreen.createRoute(
                            dropPlaceId = currentState.dropPlaceId!!,
                            dropDescription = currentState.dropQuery,
                            pickupPlaceId = currentState.pickupPlaceId!!,
                            pickupDescription = pickupDesc
                        )
                    ))
            }
        }
    }
}