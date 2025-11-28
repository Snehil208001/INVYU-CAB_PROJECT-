package com.example.invyucab_project.mainui.splashscreen_loggedin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.models.RiderOngoingRideItem
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.usecase.GetOngoingRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class to define where the Splash screen should navigate
sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object Auth : SplashDestination()
    object Home : SplashDestination()
    object Driver : SplashDestination()
    object Admin : SplashDestination()
    data class BookingDetail(val ride: RiderOngoingRideItem) : SplashDestination()
}

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getOngoingRideUseCase: GetOngoingRideUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<SplashDestination>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun checkStartDestination() {
        viewModelScope.launch {
            // 1. Maintain the Splash Delay for branding
            delay(1500L)

            // 2. Check if user is logged in locally
            if (!isUserLoggedIn()) {
                _navigationEvent.emit(SplashDestination.Onboarding)
                return@launch
            }

            val userRole = userPreferencesRepository.getUserRole()?.lowercase()

            // 3. If the user is a Rider ("user"), check for ongoing rides from API
            if (userRole == "user") {
                checkOngoingRideForRider()
            } else {
                // 4. Navigate based on other roles
                when (userRole) {
                    "driver" -> _navigationEvent.emit(SplashDestination.Driver)
                    "admin" -> _navigationEvent.emit(SplashDestination.Admin)
                    else -> _navigationEvent.emit(SplashDestination.Home)
                }
            }
        }
    }

    private suspend fun checkOngoingRideForRider() {
        val userIdStr = userPreferencesRepository.getUserId()
        val userId = userIdStr?.toIntOrNull()

        if (userId == null) {
            _navigationEvent.emit(SplashDestination.Home)
            return
        }

        try {
            // API Call to get rides
            val response = getOngoingRideUseCase(userId)

            if (response.isSuccessful && response.body()?.success == true) {
                val rides = response.body()?.data

                // Filter for a ride that is active (Accepted, Arrived, or In Progress)
                // "requested" (searching) is excluded, so user goes to Home if just searching.
                val activeRide = rides?.find {
                    it.status == "accepted" || it.status == "in_progress" || it.status == "arrived"
                }

                if (activeRide != null) {
                    // ✅ Found active ride: Navigate to BookingDetailScreen
                    _navigationEvent.emit(SplashDestination.BookingDetail(activeRide))
                } else {
                    // No active ride: Navigate to Home
                    _navigationEvent.emit(SplashDestination.Home)
                }
            } else {
                // API fail or empty: Default to Home
                _navigationEvent.emit(SplashDestination.Home)
            }
        } catch (e: Exception) {
            Log.e("SplashScreen", "Error checking rides: ${e.message}")
            // On error (e.g. offline), default to Home.
            // Note: If strictly needed, you could retry or show error, but Home is safe.
            _navigationEvent.emit(SplashDestination.Home)
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val status = userPreferencesRepository.getUserStatus()
        val userId = userPreferencesRepository.getUserId()

        val isValid = status == "active" && !userId.isNullOrBlank()

        if (status == "active" && !isValid) {
            userPreferencesRepository.clearUserStatus()
        }

        return isValid
    }

    fun getUserRole(): String? {
        return userPreferencesRepository.getUserRole()
    }
}