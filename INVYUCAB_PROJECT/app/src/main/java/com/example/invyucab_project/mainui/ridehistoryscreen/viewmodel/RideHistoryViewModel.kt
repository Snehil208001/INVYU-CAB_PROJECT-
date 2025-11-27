package com.example.invyucab_project.mainui.ridehistoryscreen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.models.RiderRideHistoryItem
import com.example.invyucab_project.domain.usecase.GetRideHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val getRideHistoryUseCase: GetRideHistoryUseCase
) : ViewModel() {

    // ✅ UPDATED: Uses RiderRideHistoryItem
    private val _rideHistory = MutableStateFlow<List<RiderRideHistoryItem>>(emptyList())
    val rideHistory: StateFlow<List<RiderRideHistoryItem>> = _rideHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchRideHistory()
    }

    fun fetchRideHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = getRideHistoryUseCase.invoke(userId = 1)
                if (response.isSuccessful && response.body()?.success == true) {
                    _rideHistory.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Failed to load ride history"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("RideHistoryVM", "Error fetching history", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}