package com.example.invyucab_project.mainui.locationsearchscreen.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.domain.model.EditingField
import com.example.invyucab_project.domain.model.SearchLocation
import com.example.invyucab_project.domain.usecase.GetAutocompletePredictionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val getAutocompletePredictionsUseCase: GetAutocompletePredictionsUseCase
) : ViewModel() { // ⬅️ Does not inherit from BaseViewModel, handles its own state

    var pickupDescription by mutableStateOf("Your Current Location")
        private set
    var pickupPlaceId by mutableStateOf<String?>("current_location")
        private set

    var dropDescription by mutableStateOf("")
        private set
    var dropPlaceId by mutableStateOf<String?>(null)
        private set

    var activeField by mutableStateOf(EditingField.DROP)
        private set

    // Simple loading/error state for this specific screen
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _searchResults = MutableStateFlow<List<SearchLocation>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null
    //                              ↓↓↓↓↓↓↓
    private var sessionToken: String = UUID.randomUUID().toString() // <-- THIS IS THE FIX

    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        errorMessage = null

        if (activeField == EditingField.PICKUP) {
            pickupDescription = query
            if (query != "Your Current Location") pickupPlaceId = null
        } else {
            dropDescription = query
            dropPlaceId = null
        }

        if (query.length < 3) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce

            getAutocompletePredictionsUseCase.invoke(query, sessionToken).onEach { result ->
                when (result) {
                    is Resource.Loading -> isLoading = true
                    is Resource.Success -> {
                        isLoading = false
                        _searchResults.value = result.data?.map {
                            SearchLocation(
                                name = it.structuredFormatting.mainText,
                                address = it.structuredFormatting.secondaryText ?: "",
                                icon = Icons.Default.LocationOn,
                                placeId = it.placeId
                            )
                        } ?: emptyList()
                    }
                    is Resource.Error -> {
                        isLoading = false
                        errorMessage = result.message
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onFieldActivated(field: EditingField) {
        activeField = field
        if (field == EditingField.PICKUP && pickupPlaceId == "current_location") {
            pickupDescription = ""
            pickupPlaceId = null
        }
        _searchResults.value = emptyList()
        errorMessage = null
    }

    fun onFieldFocusLost(field: EditingField) {
        if (field == EditingField.PICKUP) {
            if (pickupDescription.isBlank()) {
                pickupDescription = "Your Current Location"
                pickupPlaceId = "current_location"
            }
        }
    }

    fun onSearchResultClicked(location: SearchLocation) {
        if (activeField == EditingField.PICKUP) {
            pickupDescription = location.name
            pickupPlaceId = location.placeId
            activeField = EditingField.DROP
        } else {
            dropDescription = location.name
            dropPlaceId = location.placeId
            if (pickupPlaceId == null) {
                activeField = EditingField.PICKUP
            }
        }
        _searchResults.value = emptyList()
        resetSessionToken()
    }

    fun onClearField(field: EditingField) {
        if (field == EditingField.PICKUP) {
            pickupDescription = ""
            pickupPlaceId = null
        } else {
            dropDescription = ""
            dropPlaceId = null
        }
        _searchResults.value = emptyList()
        errorMessage = null
    }

    fun resetSessionToken() {
        sessionToken = UUID.randomUUID().toString()
    }
}