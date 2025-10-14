package com.example.invyucab_project.mainui.authscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class AuthTab {
    SIGN_UP,
    SIGN_IN
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    var selectedTab by mutableStateOf(AuthTab.SIGN_UP)
        private set

    var signUpEmail by mutableStateOf("")
        private set

    var signUpPhone by mutableStateOf("")
        private set

    var signInPhone by mutableStateOf("")
        private set

    fun onTabSelected(tab: AuthTab) {
        selectedTab = tab
    }

    fun onSignUpEmailChange(value: String) {
        signUpEmail = value
    }

    fun onSignUpPhoneChange(value: String) {
        signUpPhone = value
    }

    fun onSignInPhoneChange(value: String) {
        signInPhone = value
    }

    // ... (inside AuthViewModel)
    fun onSignUpClicked(onNavigate: (String) -> Unit) {
        // TODO: Implement actual Sign Up API call and OTP sending
        // On success, navigate to OTP screen
        if (signUpPhone.isNotBlank()) {
            onNavigate(signUpPhone)
        }
    }

    fun onSignInClicked(onNavigate: (String) -> Unit) {
        // TODO: Implement actual Sign In API call and OTP sending
        // On success, navigate to OTP screen
        if (signInPhone.isNotBlank()) {
            onNavigate(signInPhone)
        }
    }
// ...
}