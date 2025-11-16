package com.example.invyucab_project.mainui.vehiclepreferences.viewmodel


import android.util.Log // ✅ --- IMPORT ADDED ---
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.AddVehicleRequest
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.usecase.AddVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehiclePreferencesViewModel @Inject constructor(
    private val addVehicleUseCase: AddVehicleUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : BaseViewModel() {

    private val _vehicleNumber = MutableStateFlow("")
    val vehicleNumber = _vehicleNumber.asStateFlow()

    private val _model = MutableStateFlow("")
    val model = _model.asStateFlow()

    private val _type = MutableStateFlow("")
    val type = _type.asStateFlow()

    private val _color = MutableStateFlow("")
    val color = _color.asStateFlow()

    private val _capacity = MutableStateFlow("")
    val capacity = _capacity.asStateFlow()

    private val _isTypeDropdownExpanded = MutableStateFlow(false)
    val isTypeDropdownExpanded = _isTypeDropdownExpanded.asStateFlow()

    val vehicleTypes = listOf("bike", "auto", "car")


    fun onVehicleNumberChange(newValue: String) {
        _vehicleNumber.update { newValue }
    }
    fun onModelChange(newValue: String) {
        _model.update { newValue }
    }
    fun onTypeChange(newValue: String) {
        _type.update { newValue }
        _isTypeDropdownExpanded.value = false
    }
    fun onSetTypeDropdownExpanded(isExpanded: Boolean) {
        _isTypeDropdownExpanded.value = isExpanded
    }
    fun onColorChange(newValue: String) {
        _color.update { newValue }
    }
    fun onCapacityChange(newValue: String) {
        _capacity.update { newValue }
    }

    fun onAddVehicleClicked() {
        // ✅ --- LOGGING ADDED ---
        Log.d("VehicleVM", "onAddVehicleClicked called.")

        val number = _vehicleNumber.value
        val model = _model.value
        val type = _type.value
        val color = _color.value
        val capacity = _capacity.value

        // Simple validation
        if (number.isBlank() || model.isBlank() || type.isBlank() || color.isBlank() || capacity.isBlank()) {
            _apiError.value = "All fields are required"
            // ✅ --- LOGGING ADDED ---
            Log.e("VehicleVM", "Validation failed: All fields are required.")
            return
        }

        // ✅ --- Set loading to true immediately ---
        _isLoading.value = true

        viewModelScope.launch {
            val driverId = userPreferencesRepository.getUserId()

            if (driverId == null) {
                _apiError.value = "Could not find user ID. Please log in again."
                // ✅ --- LOGGING AND LOADING FIX ---
                Log.e("VehicleVM", "Validation failed: driverId is null.")
                _isLoading.value = false // Stop loading
                return@launch
            }

            // ✅ --- LOGGING ADDED ---
            Log.d("VehicleVM", "Validation passed. Driver ID: $driverId. Calling use case...")

            val request = AddVehicleRequest(
                driverId = driverId,
                vehicleNumber = number,
                model = model,
                type = type,
                color = color,
                capacity = capacity
            )

            addVehicleUseCase(request).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already loading
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        sendEvent(UiEvent.ShowSnackbar("Vehicle Added Successfully!"))
                        sendEvent(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _apiError.value = result.message ?: "An unknown error occurred"
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}