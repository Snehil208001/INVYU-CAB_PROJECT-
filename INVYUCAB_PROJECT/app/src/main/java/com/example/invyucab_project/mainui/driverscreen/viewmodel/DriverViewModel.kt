package com.example.invyucab_project.mainui.driverscreen.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.usecase.GetVehicleDetailsUseCase
import com.example.invyucab_project.domain.usecase.LogoutUserUseCase
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- ✅ ADDED: Simple Data Model for Ride Requests in the UI ---
data class RideRequestItem(
    val rideId: Int,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropLat: Double,
    val dropLng: Double,
    val price: Double,
    val pickupAddress: String = "fetching address...",
    val dropAddress: String = "fetching address..."
)

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val logoutUserUseCase: LogoutUserUseCase,
    // --- Injected Location Services ---
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationManager: LocationManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getVehicleDetailsUseCase: GetVehicleDetailsUseCase,
    // private val appRepository: AppRepository // ✅ TODO: Inject Repository here for API calls
) : BaseViewModel() {

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    // --- State for the map's camera ---
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    // --- State for the vehicle banner ---
    private val _showVehicleBanner = MutableStateFlow(false)
    val showVehicleBanner: StateFlow<Boolean> = _showVehicleBanner.asStateFlow()

    // --- ✅ ADDED: State for Incoming Ride Requests ---
    private val _rideRequests = MutableStateFlow<List<RideRequestItem>>(emptyList())
    val rideRequests: StateFlow<List<RideRequestItem>> = _rideRequests.asStateFlow()

    private var pollingJob: Job? = null

    init {
        // Get location as soon as the ViewModel is created
        getCurrentLocation()
    }

    // ✅ --- FIX: 'private' is removed to allow the screen to call this. ---
    fun checkVehicleDetails() {
        viewModelScope.launch {
            // Use getUserId() as this is what we save during sign-in/sign-up
            val driverId = userPreferencesRepository.getUserId()

            if (driverId == null) {
                Log.w("DriverViewModel", "Driver ID not found in preferences. Showing banner.")
                _showVehicleBanner.value = true
                return@launch
            }

            Log.d("DriverViewModel", "Checking vehicle for driverId: $driverId")
            getVehicleDetailsUseCase(driverId).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Don't need to show full-screen loader for this
                    }
                    is Resource.Success -> {
                        if (result.data == null) {
                            // This means no vehicle is registered
                            Log.d("DriverViewModel", "No vehicle found. Showing banner.")
                            _showVehicleBanner.value = true
                        } else {
                            // Vehicle found, hide banner
                            Log.d("DriverViewModel", "Vehicle found. Hiding banner.")
                            _showVehicleBanner.value = false
                        }
                    }
                    is Resource.Error -> {
                        // Per your request: If the API fails, show the banner
                        // so the user can add a vehicle.
                        Log.e("DriverViewModel", "API Error checking vehicle: ${result.message}. Showing banner.")
                        _apiError.value = result.message ?: "Could not verify vehicle"
                        _showVehicleBanner.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
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
                    // ✅✅✅ START OF FIX ✅✅✅
                    // Corrected the typo from 'location.L' to 'location.longitude'
                    _currentLocation.value = LatLng(location.latitude, location.longitude)
                    // ✅✅✅ END OF FIX ✅✅✅
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
        if (active) {
            Log.d("DriverViewModel", "Driver is now ACTIVE. Starting to look for rides...")
            // ✅ ADDED: Start polling for rides when active
            startLookingForRides()
        } else {
            Log.d("DriverViewModel", "Driver is now INACTIVE. Stopping search.")
            // ✅ ADDED: Stop polling
            stopLookingForRides()
            _rideRequests.value = emptyList() // Clear old requests
        }
    }

    // --- ✅ ADDED: Polling Logic to Fetch Available Rides ---
    private fun startLookingForRides() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (_isActive.value) {
                try {
                    // ✅ TODO: Call your real API here
                    // val response = appRepository.getAvailableRides()

                    // --- MOCK DATA FOR DEMONSTRATION (Remove this when API is ready) ---
                    val mockRide = RideRequestItem(
                        rideId = 101,
                        pickupLat = 25.61, pickupLng = 85.14,
                        dropLat = 25.59, dropLng = 85.13,
                        price = 250.0,
                        pickupAddress = "Bailey Road, Patna",
                        dropAddress = "Gandhi Maidan, Patna"
                    )
                    // Simulate finding a ride every 5 seconds if list is empty
                    if (_rideRequests.value.isEmpty()) {
                        _rideRequests.value = listOf(mockRide)
                        // sendEvent(UiEvent.ShowSnackbar("New Ride Request Found!"))
                    }
                    // -----------------------------------------------------------

                } catch (e: Exception) {
                    Log.e("DriverViewModel", "Error fetching rides: ${e.message}")
                }
                delay(5000) // Check every 5 seconds
            }
        }
    }

    private fun stopLookingForRides() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // --- ✅ ADDED: Accept Ride Logic ---
    fun onAcceptRide(ride: RideRequestItem) {
        viewModelScope.launch {
            _apiError.value = "Accepting ride..." // Show simple loading indicator via error/msg

            // ✅ TODO: Call API to accept ride
            // val driverId = userPreferencesRepository.getUserId()?.toInt()
            // val result = appRepository.acceptRide(ride.rideId, driverId)

            // For now, simulate success:
            delay(1000)
            stopLookingForRides() // Stop looking for new ones
            _rideRequests.value = emptyList() // Clear the list

            // Navigate to a "Navigation to Pickup" screen (You can create this later)
            // sendEvent(UiEvent.Navigate(Screen.NavigationScreen.route))
            sendEvent(UiEvent.ShowSnackbar("Ride Accepted! Navigate to pickup."))
        }
    }

    fun onDeclineRide(ride: RideRequestItem) {
        // Remove this specific ride from the list locally
        _rideRequests.value = _rideRequests.value.filter { it.rideId != ride.rideId }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUserUseCase()
            // Send navigation event using the BaseViewModel's eventFlow
            sendEvent(UiEvent.Navigate(Screen.AuthScreen.route))
        }
    }
}