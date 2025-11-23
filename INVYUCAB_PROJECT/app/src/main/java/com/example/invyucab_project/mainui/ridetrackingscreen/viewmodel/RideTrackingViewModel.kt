package com.example.invyucab_project.mainui.ridetrackingscreen.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.data.models.StartRideRequest
import com.example.invyucab_project.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RideTrackingViewModel @Inject constructor(
    private val appRepository: AppRepository
) : BaseViewModel() {

    val startRideSuccess = mutableStateOf(false)

    fun startRide(rideId: Int, riderId: Int, driverId: Int, otp: String) {
        viewModelScope.launch {
            try {
                // Current timestamp
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDateAndTime: String = sdf.format(Date())

                val request = StartRideRequest(
                    rideId = rideId,
                    riderId = riderId,
                    driverId = driverId,
                    userPin = otp.toIntOrNull() ?: 0,
                    startedAt = currentDateAndTime
                )

                val response = appRepository.startRideFromDriverSide(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    startRideSuccess.value = true
                    sendEvent(UiEvent.ShowSnackbar(response.body()?.message ?: "Ride Started Successfully!"))
                } else {
                    sendEvent(UiEvent.ShowSnackbar(response.body()?.message ?: "Failed to start ride"))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }
}