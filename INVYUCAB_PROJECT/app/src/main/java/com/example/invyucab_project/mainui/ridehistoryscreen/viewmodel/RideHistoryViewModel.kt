package com.example.invyucab_project.mainui.ridehistoryscreen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.models.RiderRideHistoryItem
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.usecase.GetRideHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val getRideHistoryUseCase: GetRideHistoryUseCase,
    private val userPreferencesRepository: UserPreferencesRepository // ✅ Inject Preferences
) : ViewModel() {

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
        // ✅ Get the actual logged-in User ID
        val userIdStr = userPreferencesRepository.getUserId()
        val userId = userIdStr?.toIntOrNull()

        if (userId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // ✅ Pass the dynamic userId
                val response = getRideHistoryUseCase.invoke(userId = userId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val history = response.body()?.data ?: emptyList()
                    // ✅ Reverse the list to show Newest rides first
                    _rideHistory.value = history.reversed()
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