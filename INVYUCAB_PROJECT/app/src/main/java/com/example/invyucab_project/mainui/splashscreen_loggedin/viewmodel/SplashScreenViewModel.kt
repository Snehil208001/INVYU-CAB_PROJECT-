package com.example.invyucab_project.mainui.splashscreen_loggedin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.models.RiderOngoingRideItem
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.usecase.GetOngoingRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getOngoingRideUseCase: GetOngoingRideUseCase // ✅ Inject UseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<RiderOngoingRideItem?>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    /**
     * Checks if the user's status is "active".
     */
    fun isUserLoggedIn(): Boolean {
        val status = userPreferencesRepository.getUserStatus()
        val userId = userPreferencesRepository.getUserId()

        val isValid = status == "active" && !userId.isNullOrBlank()

        if (status == "active" && !isValid) {
            Log.e("SplashScreen", "❌ CORRUPT STATE: User is active but has no ID. Clearing session.")
            userPreferencesRepository.clearUserStatus()
        }

        return isValid
    }

    fun getUserRole(): String? {
        return userPreferencesRepository.getUserRole()
    }

    // ✅ NEW: Check for ongoing rides logic
    fun checkOngoingRide() {
        val userIdStr = userPreferencesRepository.getUserId()
        val userId = userIdStr?.toIntOrNull()

        if (userId == null) {
            // No valid ID, emit null to proceed to home/login normally
            emitNullNavigation()
            return
        }

        viewModelScope.launch {
            try {
                val response = getOngoingRideUseCase(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val rides = response.body()?.data
                    // Check if there is an active ride
                    val activeRide = rides?.find {
                        it.status == "accepted" || it.status == "in_progress" || it.status == "arrived"
                    }

                    if (activeRide != null) {
                        _navigationEvent.emit(activeRide)
                    } else {
                        // No active ride found
                        _navigationEvent.emit(null)
                    }
                } else {
                    _navigationEvent.emit(null)
                }
            } catch (e: Exception) {
                Log.e("SplashScreen", "Error checking rides: ${e.message}")
                _navigationEvent.emit(null)
            }
        }
    }

    private fun emitNullNavigation() {
        viewModelScope.launch {
            _navigationEvent.emit(null)
        }
    }
}