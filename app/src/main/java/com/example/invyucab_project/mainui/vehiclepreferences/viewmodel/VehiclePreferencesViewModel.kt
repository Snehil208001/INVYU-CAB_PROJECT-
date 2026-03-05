package com.example.invyucab_project.mainui.vehiclepreferences.viewmodel


import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.data.models.AddVehicleRequest
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
import com.example.invyucab_project.domain.usecase.AddVehicleUseCase
// ✅ --- IMPORT ADDED ---
import com.example.invyucab_project.domain.usecase.GetVehicleDetailsUseCase
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
    private val userPreferencesRepository: UserPreferencesRepository,
    // ✅ --- DEPENDENCY INJECTED ---
    private val getVehicleDetailsUseCase: GetVehicleDetailsUseCase
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

    // ✅ --- NEW STATE FOR BUTTON TEXT ---
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    val vehicleTypes = listOf("bike", "auto", "car")

    // ✅ --- INIT BLOCK ADDED ---
    init {
        loadExistingVehicleDetails()
    }

    // ✅ --- NEW FUNCTION TO LOAD DATA ---
    private fun loadExistingVehicleDetails() {
        viewModelScope.launch {
            val driverId = userPreferencesRepository.getUserId()
            if (driverId == null) {
                Log.w("VehicleVM", "No Driver ID found, skipping vehicle load.")
                // User will just see an empty "add" form, which is correct
                return@launch
            }

            Log.d("VehicleVM", "Loading existing vehicle details for driver: $driverId")
            getVehicleDetailsUseCase(driverId).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        result.data?.let { vehicle ->
                            // Vehicle found, populate the form fields
                            Log.d("VehicleVM", "Vehicle found: $vehicle. Populating fields.")

                            // Use Elvis operator (?:) to provide a default empty string if null
                            _vehicleNumber.update { vehicle.vehicleNumber ?: "" }
                            _model.update { vehicle.model ?: "" }
                            _type.update { vehicle.type ?: "" }
                            _color.update { vehicle.color ?: "" }
                            _capacity.update { vehicle.capacity ?: "" }

                            // Set mode to "edit"
                            _isEditMode.value = true
                        } ?: run {
                            // No vehicle found, user will see the empty "add" form
                            Log.d("VehicleVM", "No existing vehicle registered.")
                            _isEditMode.value = false
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        // Don't block the user, just log the error.
                        // They can still try to add a vehicle.
                        Log.e("VehicleVM", "Error loading vehicle details: ${result.message}")
                        _apiError.value = "Couldn't load existing details. You can add a new one."
                        _isEditMode.value = false
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

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
        // This function now handles both "Add" and "Update"
        Log.d("VehicleVM", "onAddVehicleClicked called. Mode: ${if (_isEditMode.value) "Update" else "Add"}")

        val number = _vehicleNumber.value
        val model = _model.value
        val type = _type.value
        val color = _color.value
        val capacity = _capacity.value

        // Simple validation
        if (number.isBlank() || model.isBlank() || type.isBlank() || color.isBlank() || capacity.isBlank()) {
            _apiError.value = "All fields are required"
            Log.e("VehicleVM", "Validation failed: All fields are required.")
            return
        }

        // ✅ --- THIS IS WHERE THE LOADER STARTS ---
        _isLoading.value = true

        viewModelScope.launch {
            val driverId = userPreferencesRepository.getUserId()

            if (driverId == null) {
                _apiError.value = "Could not find user ID. Please log in again."
                Log.e("VehicleVM", "Validation failed: driverId is null.")
                _isLoading.value = false // Stop loading
                return@launch
            }

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
                        // ✅ --- THIS IS WHERE THE LOADER STOPS ---
                        _isLoading.value = false
                        val message = if (_isEditMode.value) "Vehicle Updated Successfully!" else "Vehicle Added Successfully!"
                        sendEvent(UiEvent.ShowSnackbar(message))
                        sendEvent(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> {
                        // ✅ --- THIS IS WHERE THE LOADER STOPS ---
                        _isLoading.value = false
                        _apiError.value = result.message ?: "An unknown error occurred"
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}