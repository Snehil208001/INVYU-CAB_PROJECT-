package com.example.invyucab_project.mainui.driverscreen.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.usecase.LogoutUserUseCase
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase,
    // --- Injected Location Services ---
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationManager: LocationManager
) : BaseViewModel() {

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    // --- State for the map's camera ---
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    init {
        // Get location as soon as the ViewModel is created
        getCurrentLocation()
    }

    // --- Location Logic (from HomeViewModel) ---

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (!isLocationEnabled()) {
            _apiError.value = "Please turn on location services (GPS)."
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                _currentLocation.value = LatLng(location.latitude, location.longitude)
            } else {
                requestNewLocation()
            }
        }.addOnFailureListener {
            _apiError.value = "Failed to get location. Please enable GPS."
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        if (!isLocationEnabled()) {
            _apiError.value = "Please turn on location services (GPS)."
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
                    _currentLocation.value = LatLng(location.latitude, location.longitude)
                } ?: run {
                    _apiError.value = "Could not fetch current location."
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    // --- Screen Logic ---

    fun onActiveToggleChanged(active: Boolean) {
        _isActive.value = active
        viewModelScope.launch {
            if (active) {
                Log.d("DriverViewModel", "Driver is now ACTIVE")
                // TODO: Call API to set driver status to "active"
            } else {
                Log.d("DriverViewModel", "Driver is now INACTIVE")
                // TODO: Call API to set driver status to "inactive"
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUserUseCase()
            // Send navigation event using the BaseViewModel's eventFlow
            sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
        }
    }
}