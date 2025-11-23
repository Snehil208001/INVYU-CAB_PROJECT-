package com.example.invyucab_project.mainui.ridebookingscreen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.RideBookingUiState
import com.example.invyucab_project.domain.usecase.GetDirectionsAndRouteUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class RideBookingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDirectionsAndRouteUseCase: GetDirectionsAndRouteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideBookingUiState())
    val uiState = _uiState.asStateFlow()

    // Retrieve arguments passed from Navigation
    private val rideId: String? = savedStateHandle.get<Int>("rideId")?.toString()

    // ✅ Retrieve userPin
    private val userPin: String? = savedStateHandle.get<Int>("userPin")?.toString()

    private val pickupLat: Double? = savedStateHandle.get<String>("pickupLat")?.toDoubleOrNull()
    private val pickupLng: Double? = savedStateHandle.get<String>("pickupLng")?.toDoubleOrNull()
    private val dropLat: Double? = savedStateHandle.get<String>("dropLat")?.toDoubleOrNull()
    private val dropLng: Double? = savedStateHandle.get<String>("dropLng")?.toDoubleOrNull()

    // ✅ Get the Raw Encoded Strings
    private val rawPickupAddress: String? = savedStateHandle.get<String>("pickupAddress")
    private val rawDropAddress: String? = savedStateHandle.get<String>("dropAddress")

    private val dropPlaceId: String? = savedStateHandle.get<String>("dropPlaceId")

    init {
        initializeRideDetails()
    }

    private fun initializeRideDetails() {
        val pickupLocation = if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null
        val dropLocation = if (dropLat != null && dropLng != null) LatLng(dropLat, dropLng) else null

        // ✅ Decode the addresses
        val pickupAddress = decodeString(rawPickupAddress)
        val dropAddress = decodeString(rawDropAddress)

        _uiState.update {
            it.copy(
                rideId = rideId,
                userPin = userPin, // ✅ Set PIN in state
                pickupLocation = pickupLocation,
                dropLocation = dropLocation,
                pickupDescription = pickupAddress ?: "Pickup Location",
                dropDescription = dropAddress ?: "Drop Location",
                isLoading = true
            )
        }

        if (pickupLocation != null && dropPlaceId != null) {
            fetchRoute(pickupLocation, dropPlaceId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun fetchRoute(pickup: LatLng, dropId: String) {
        viewModelScope.launch {
            getDirectionsAndRouteUseCase(pickup, dropId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                routePolyline = result.data?.polyline ?: emptyList(),
                                isSearchingForDriver = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onCancelRide() {
        // TODO: Add logic to cancel the ride via API
        _uiState.update { it.copy(isSearchingForDriver = false) }
    }

    private fun decodeString(encoded: String?): String? {
        if (encoded == null) return null
        return try {
            URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            encoded
        }
    }
}