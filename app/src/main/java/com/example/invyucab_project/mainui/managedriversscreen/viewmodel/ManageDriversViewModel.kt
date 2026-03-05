package com.example.invyucab_project.mainui.managedriversscreen.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriverUiModel(
    val id: Int,
    val name: String,
    val phone: String,
    val vehicleModel: String,
    val vehicleNumber: String,
    val status: String, // "Active", "Pending", "Blocked"
    val rating: Double
)

@HiltViewModel
class ManageDriversViewModel @Inject constructor() : BaseViewModel() {

    private val _drivers = mutableStateOf<List<DriverUiModel>>(emptyList())
    val drivers: State<List<DriverUiModel>> = _drivers

    private val _filteredDrivers = mutableStateOf<List<DriverUiModel>>(emptyList())
    val filteredDrivers: State<List<DriverUiModel>> = _filteredDrivers

    private val _selectedFilter = mutableStateOf("All")
    val selectedFilter: State<String> = _selectedFilter

    init {
        loadDrivers()
    }

    private fun loadDrivers() {
        _isLoading.value = true
        viewModelScope.launch {
            // SIMULATING NETWORK DELAY
            delay(1000)

            // MOCK DATA - Replace with actual Repository call later
            val mockList = listOf(
                DriverUiModel(1, "Ramesh Kumar", "+91 9876543210", "Swift Dzire", "BR-01-AB-1234", "Active", 4.8),
                DriverUiModel(2, "Suresh Singh", "+91 9876500000", "Honda City", "BR-01-XY-9876", "Pending", 0.0),
                DriverUiModel(3, "Mahesh Yadav", "+91 9998887776", "Tata Nexon", "BR-01-ZZ-1122", "Active", 4.5),
                DriverUiModel(4, "Dinesh Verma", "+91 8887776665", "Mahindra XUV", "BR-01-MN-4567", "Blocked", 3.2),
                DriverUiModel(5, "Rajesh Gupta", "+91 7776665554", "Maruti Alto", "BR-01-PQ-3344", "Pending", 0.0)
            )

            _drivers.value = mockList
            applyFilter(_selectedFilter.value)
            _isLoading.value = false
        }
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        applyFilter(filter)
    }

    private fun applyFilter(filter: String) {
        if (filter == "All") {
            _filteredDrivers.value = _drivers.value
        } else {
            _filteredDrivers.value = _drivers.value.filter { it.status == filter }
        }
    }

    fun onApproveClicked(driver: DriverUiModel) {
        // Logic to approve driver API
        sendEvent(UiEvent.ShowSnackbar("${driver.name} Approved successfully."))
        // Update local state to reflect change
        updateDriverStatus(driver.id, "Active")
    }

    fun onBlockClicked(driver: DriverUiModel) {
        sendEvent(UiEvent.ShowSnackbar("${driver.name} has been blocked."))
        updateDriverStatus(driver.id, "Blocked")
    }

    private fun updateDriverStatus(id: Int, newStatus: String) {
        val updatedList = _drivers.value.map {
            if (it.id == id) it.copy(status = newStatus) else it
        }
        _drivers.value = updatedList
        applyFilter(_selectedFilter.value)
    }

    fun onBackClicked() {
        // Navigate back handled by NavController in UI, but we can track analytics here
    }
}