package com.example.invyucab_project.mainui.loginscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor() : ViewModel() {
    // State variables that the UI will observe
    var emailOrPhone by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isUser by mutableStateOf(true)
        private set

    // Event handlers that the UI will call
    fun onEmailOrPhoneChange(value: String) {
        emailOrPhone = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun onRoleChange(isUserRole: Boolean) {
        isUser = isUserRole
    }

    fun onLoginClicked() {
        // TODO: Implement your login logic here
        // For example, validate inputs and make a network call
    }
}