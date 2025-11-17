package com.example.invyucab_project.mainui.authscreen.viewmodel

// ✅ --- START OF FIX: IMPORTS ADDED ---
import android.util.Log
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
// ✅ --- END OF FIX ---
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.model.AuthTab
import com.example.invyucab_project.domain.model.GoogleSignInState
import com.example.invyucab_project.domain.usecase.CheckUserUseCase
import com.example.invyucab_project.domain.usecase.UserCheckStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val checkUserUseCase: CheckUserUseCase,
    // ✅ --- START OF FIX: REPOSITORY INJECTED ---
    private val userPreferencesRepository: UserPreferencesRepository
    // ✅ --- END OF FIX ---
) : BaseViewModel() {

    // ✅ --- START OF FIX: TAG ADDED FOR LOGGING ---
    private val TAG = "AuthViewModel"
    // ✅ --- END OF FIX ---

    var selectedTab by mutableStateOf(AuthTab.SIGN_UP)
        private set

    var signUpPhone by mutableStateOf("")
        private set
    var signUpPhoneError by mutableStateOf<String?>(null)
        private set

    var signInPhone by mutableStateOf("")
        private set
    var signInPhoneError by mutableStateOf<String?>(null)
        private set

    private val _googleSignInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: StateFlow<GoogleSignInState> = _googleSignInState.asStateFlow()

    fun onTabSelected(tab: AuthTab) {
        selectedTab = tab
        signUpPhoneError = null
        signInPhoneError = null
        _apiError.value = null
        if (_googleSignInState.value is GoogleSignInState.Error) {
            resetGoogleSignInState()
        }
    }

    fun onSignUpPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signUpPhone = value
            _apiError.value = null
            if (signUpPhoneError != null) validateSignUpPhone()
        }
    }

    fun onSignInPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signInPhone = value
            _apiError.value = null
            if (signInPhoneError != null) validateSignInPhone()
        }
    }

    private fun validateSignUpPhone(): Boolean {
        if (signUpPhone.isBlank()) {
            signUpPhoneError = "Phone number is required"
            return false
        }
        if (signUpPhone.length != 10) {
            signUpPhoneError = "Must be 10 digits"
            return false
        }
        signUpPhoneError = null
        return true
    }

    private fun validateSignInPhone(): Boolean {
        if (signInPhone.isBlank()) {
            signInPhoneError = "Phone number is required"
            return false
        }
        if (signInPhone.length != 10) {
            signInPhoneError = "Must be 10 digits"
            return false
        }
        signInPhoneError = null
        return true
    }

    fun onSignUpClicked() {
        if (!validateSignUpPhone()) return

        checkUserUseCase.invoke(signUpPhone).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                    _apiError.value = null
                }
                is Resource.Success -> {
                    _isLoading.value = false
                    when (result.data) {
                        is UserCheckStatus.Exists -> {
                            _apiError.value = "This phone number is already registered. Please Sign In."
                        }
                        is UserCheckStatus.DoesNotExist -> {
                            sendEvent(UiEvent.Navigate(
                                Screen.RoleSelectionScreen.createRoute(
                                    phone = signUpPhone
                                )
                            ))
                        }
                        // ✅✅✅ START OF FIX ✅✅✅ (This was in your original code)
                        // Add an else branch to handle the 'null' case
                        else -> {
                            _apiError.value = "An unexpected error occurred."
                        }
                        // ✅✅✅ END OF FIX ✅✅✅
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _apiError.value = result.message
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onSignInClicked() {
        if (!validateSignInPhone()) return

        checkUserUseCase.invoke(signInPhone).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                    _apiError.value = null
                }
                is Resource.Success -> {
                    _isLoading.value = false
                    when (result.data) {
                        is UserCheckStatus.Exists -> {
                            // ✅ --- START OF FIX: SAVE USER ID, ROLE, AND STATUS ---
                            // This is the fix. We save the userId from the use case response.
                            val userId = result.data.userId
                            val userRole = result.data.role

                            userPreferencesRepository.saveUserId(userId.toString())
                            userPreferencesRepository.saveUserRole(userRole)
                            userPreferencesRepository.saveUserStatus("active")

                            Log.d(TAG, "User ID $userId, Role $userRole, Status 'active' saved to preferences for sign-in.")
                            // ✅ --- END OF FIX ---

                            sendEvent(UiEvent.Navigate(
                                Screen.OtpScreen.createRoute(
                                    phone = signInPhone,
                                    isSignUp = false,
                                    role = userRole, // Pass the correct role
                                    name = null,
                                    gender = null,
                                    dob = null
                                )
                            ))
                        }
                        is UserCheckStatus.DoesNotExist -> {
                            _apiError.value = "This phone number is not registered. Please Register."
                        }
                        // ✅✅✅ START OF FIX ✅✅✅ (This was in your original code)
                        // Add an else branch to handle the 'null' case
                        else -> {
                            _apiError.value = "An unexpected error occurred."
                        }
                        // ✅✅✅ END OF FIX ✅✅✅
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _apiError.value = result.message
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetGoogleSignInState() {
        _googleSignInState.value = GoogleSignInState.Idle
    }
}