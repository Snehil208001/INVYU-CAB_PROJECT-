package com.example.invyucab_project.mainui.authscreen.viewmodel

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
    private val checkUserUseCase: CheckUserUseCase
) : BaseViewModel() {

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
                        UserCheckStatus.EXISTS -> {
                            _apiError.value = "This phone number is already registered. Please Sign In."
                        }
                        UserCheckStatus.DOES_NOT_EXIST -> {
                            sendEvent(UiEvent.Navigate(
                                Screen.UserDetailsScreen.createRoute(
                                    phone = signUpPhone,
                                    email = null,
                                    name = null
                                )
                            ))
                        }
                        null -> {} // Should not happen
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
                        UserCheckStatus.EXISTS -> {
                            sendEvent(UiEvent.Navigate(
                                Screen.OtpScreen.createRoute(
                                    phone = signInPhone,
                                    isSignUp = false,
                                    email = null,
                                    name = null,
                                    gender = null,
                                    dob = null
                                )
                            ))
                        }
                        UserCheckStatus.DOES_NOT_EXIST -> {
                            _apiError.value = "This phone number is not registered. Please Register."
                        }
                        null -> {} // Should not happen
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