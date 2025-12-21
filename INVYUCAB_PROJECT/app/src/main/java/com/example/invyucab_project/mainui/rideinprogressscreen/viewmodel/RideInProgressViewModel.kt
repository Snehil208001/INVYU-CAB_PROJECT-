package com.example.invyucab_project.mainui.rideinprogressscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideInProgressViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _updateStatus = MutableStateFlow<Result<Unit>?>(null)
    val updateStatus: StateFlow<Result<Unit>?> = _updateStatus

    fun updateRideStatus(rideId: Int, status: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateRideStatus(rideId, status)
                if (response.isSuccessful && response.body()?.success == true) {
                    _updateStatus.value = Result.success(Unit)
                } else {
                    _updateStatus.value = Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                _updateStatus.value = Result.failure(e)
            }
        }
    }

    // ✅ ADDED: Function to call the other party
    fun initiateCall(targetPhone: String) {
        viewModelScope.launch {
            try {
                // We don't need to observe this heavily in UI, just fire and forget or show a toast via repository logic if needed.
                // ideally, add a SharedFlow for Events like "ShowSnackbar" similar to RideTrackingViewModel
                repository.initiateCall(targetPhone)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}