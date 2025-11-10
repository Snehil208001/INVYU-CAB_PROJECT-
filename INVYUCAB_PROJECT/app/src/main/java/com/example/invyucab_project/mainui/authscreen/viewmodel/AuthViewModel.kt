package com.example.invyucab_project.mainui.authscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel // ✅ IMPORTED BASE
import com.example.invyucab_project.domain.model.AuthTab
import com.example.invyucab_project.domain.model.GoogleSignInState
import com.example.invyucab_project.domain.usecase.CheckUserUseCase // ✅ IMPORTED USECASE
import com.example.invyucab_project.domain.usecase.UserCheckStatus // ✅ IMPORTED USECASE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    // ✅ ONLY INJECT WHAT YOU NEED
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val checkUserUseCase: CheckUserUseCase // ✅ INJECTED USECASE
    // ⛔ CustomApiService and UserPreferencesRepository are GONE!
) : BaseViewModel() { // ✅ INHERIT FROM BASEVIEWMODEL

    private val TAG = "AuthViewModel"

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

    // ⛔ 'isLoading' and 'apiError' are now inherited from BaseViewModel

    private val _googleSignInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: StateFlow<GoogleSignInState> = _googleSignInState.asStateFlow()


    fun onTabSelected(tab: AuthTab) {
        selectedTab = tab
        signUpPhoneError = null
        signInPhoneError = null
        _apiError.value = null // ✅ Use the _apiError from BaseViewModel
        if (_googleSignInState.value is GoogleSignInState.Error) {
            resetGoogleSignInState()
        }
    }

    fun onSignUpPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signUpPhone = value
            _apiError.value = null // ✅ Use the _apiError from BaseViewModel
            if (signUpPhoneError != null) {
                validateSignUpPhone()
            }
        }
    }

    fun onSignInPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signInPhone = value
            _apiError.value = null // ✅ Use the _apiError from BaseViewModel
            if (signInPhoneError != null) {
                validateSignInPhone()
            }
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


    // ✅✅✅ START OF LOGIC REFACTOR ✅✅✅
    fun onSignUpClicked(onNavigate: (String) -> Unit) {
        if (!validateSignUpPhone()) return

        _apiError.value = null // Clear any old errors

        // Use the safeLaunch wrapper from BaseViewModel
        // It will automatically set isLoading = true/false
        safeLaunch {
            val result = checkUserUseCase.invoke(signUpPhone) // ✅ SO CLEAN!

            // Handle the result from the UseCase
            result.onSuccess { status ->
                when (status) {
                    UserCheckStatus.EXISTS -> {
                        // This is an "expected" error, so we set it manually
                        _apiError.value = "This phone number is already registered. Please Sign In."
                    }
                    UserCheckStatus.DOES_NOT_EXIST -> {
                        // Success!
                        onNavigate(signUpPhone)
                    }
                }
            }
            // An "unexpected" error (like no internet) is handled by safeLaunch
            result.onFailure { error ->
                _apiError.value = error.message // Set error from the Result
            }
        }
    }

    fun onSignInClicked(onNavigate: (String) -> Unit) {
        if (!validateSignInPhone()) return

        _apiError.value = null // Clear any old errors

        safeLaunch {
            val result = checkUserUseCase.invoke(signInPhone) // ✅ SO CLEAN!

            result.onSuccess { status ->
                when (status) {
                    UserCheckStatus.EXISTS -> {
                        // Success!
                        onNavigate(signInPhone)
                    }
                    UserCheckStatus.DOES_NOT_EXIST -> {
                        // This is an "expected" error
                        _apiError.value = "This phone number is not registered. Please Register."
                    }
                }
            }
            result.onFailure { error ->
                _apiError.value = error.message
            }
        }
    }
    // ✅✅✅ END OF LOGIC REFACTOR ✅✅✅

    // --- Google Sign-In Logic (Unchanged) ---
    fun resetGoogleSignInState() {
        _googleSignInState.value = GoogleSignInState.Idle
    }
}