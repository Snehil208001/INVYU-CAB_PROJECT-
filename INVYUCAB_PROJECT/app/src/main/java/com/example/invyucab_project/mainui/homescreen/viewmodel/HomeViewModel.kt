package com.example.invyucab_project.mainui.homescreen.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.models.Prediction
import com.example.invyucab_project.domain.model.AutocompletePrediction
import com.example.invyucab_project.domain.model.HomeUiState
import com.example.invyucab_project.domain.model.SearchField
import com.example.invyucab_project.domain.usecase.GetAutocompletePredictionsUseCase
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val getAutocompletePredictionsUseCase: GetAutocompletePredictionsUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        getCurrentLocation()
    }

    // Gets location for search biasing
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
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

    // --- Search Logic ---

    fun onPickupQueryChange(query: String) {
        _uiState.update { it.copy(pickupQuery = query, activeField = SearchField.PICKUP) }

        // ✅ MODIFIED: This logic is now simpler
        if (query.isBlank()) {
            // If query is blank (e.g., user backspaced), don't reset, just show no results
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

    // ✅ MODIFIED: This logic is now correct
    fun onClearPickup() {
        // When user taps 'X' on a selected location, clear it and focus the field.
        _uiState.update { it.copy(pickupQuery = "", pickupPlaceId = null, pickupResults = emptyList(), activeField = SearchField.PICKUP) }
    }

    fun onClearDrop() {
        _uiState.update { it.copy(dropQuery = "", dropPlaceId = null, dropResults = emptyList()) }
    }

    // ✅ MODIFIED: This is the key fix
    fun onFocusChange(field: SearchField) {
        if (field == SearchField.PICKUP && _uiState.value.pickupQuery == "Your Current Location") {
            // When user taps the "Your Current Location" text, clear it for them
            _uiState.update { it.copy(activeField = field, pickupQuery = "") }
        } else {
            _uiState.update { it.copy(activeField = field) }
        }
    }

    // ✅ ADDED: New function to handle focus loss
    fun onFocusLost(field: SearchField) {
        if (field == SearchField.PICKUP) {
            if (_uiState.value.pickupQuery.isBlank()) {
                // If user taps away from a blank pickup field, restore "Your Current Location"
                _uiState.update { it.copy(pickupQuery = "Your Current Location", pickupPlaceId = "current_location") }
            }
        }
        // No special logic needed for drop field
    }


    private fun search(query: String, field: SearchField) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L) // Debounce

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
                                    secondaryText = prediction.structuredFormatting.secondaryText ?: "", // <-- THIS IS THE FIX
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
        // Update the correct field
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

    // This function is still correct from the previous step
    fun onContinueClicked() {
        val currentState = _uiState.value

        // Ensure both IDs are valid before navigating
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