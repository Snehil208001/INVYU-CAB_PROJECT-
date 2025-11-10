package com.example.invyucab_project.mainui.roleselectionscreen.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
// import androidx.lifecycle.viewModelScope // ❌ No longer needed
import com.example.invyucab_project.core.base.BaseViewModel
// import com.example.invyucab_project.core.common.Resource // ❌ No longer needed
import com.example.invyucab_project.core.navigations.Screen
// import com.example.invyucab_project.data.models.CreateUserRequest // ❌ No longer needed
// import com.example.invyucab_project.domain.usecase.CreateUserUseCase // ❌ No longer needed
// import com.example.invyucab_project.domain.usecase.SaveUserStatusUseCase // ❌ No longer needed
import dagger.hilt.android.lifecycle.HiltViewModel
// import kotlinx.coroutines.flow.launchIn // ❌ No longer needed
// import kotlinx.coroutines.flow.onEach // ❌ No longer needed
// import kotlinx.coroutines.launch // ❌ No longer needed
// import java.net.URLDecoder // ❌ No longer needed
// import java.nio.charset.StandardCharsets // ❌ No longer needed
// import java.text.SimpleDateFormat // ❌ No longer needed
// import java.util.Locale // ❌ No longer needed
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    // ❌ Usecases removed, no longer creating user here
    // private val createUserUseCase: CreateUserUseCase,
    // private val saveUserStatusUseCase: SaveUserStatusUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // ✅ MODIFIED: Only phone is needed
    val phone: String? = savedStateHandle.get<String>("phone")

    // ❌ Removed email, name, gender, dob, etc.

    init {
        Log.d("RoleSelectionViewModel", "Received data: Phone=$phone")
    }

    // ❌ Removed formatDobForApi

    fun onRoleSelected(role: String) {
        _apiError.value = null
        _isLoading.value = true // <-- Set to true

        Log.d("RoleSelectionViewModel", "User selected role: $role. Navigating to UserDetails...")

        sendEvent(UiEvent.Navigate(
            Screen.UserDetailsScreen.createRoute(
                phone = phone!!,
                role = role, // Pass "Rider" or "Driver"
                email = null,
                name = null
            )
        ))

        // ✅✅✅ THIS IS THE FIX ✅✅✅
        // Reset the loading state immediately after sending the event.
        // When you navigate back to this screen, it will no longer be loading.
        _isLoading.value = false
        // ✅✅✅ END OF FIX ✅✅✅
    }
}