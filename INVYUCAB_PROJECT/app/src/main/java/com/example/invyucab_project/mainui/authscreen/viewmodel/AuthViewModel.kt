package com.example.invyucab_project.mainui.authscreen.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.models.CheckUserRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

enum class AuthTab {
    SIGN_UP,
    SIGN_IN
}

sealed class GoogleSignInState {
    data object Idle : GoogleSignInState()
    data object Loading : GoogleSignInState()
    data class Success(val user: FirebaseUser, val isNewUser: Boolean) : GoogleSignInState()
    data class Error(val message: String) : GoogleSignInState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    private val customApiService: CustomApiService // ✅ INJECTED
) : ViewModel() {

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

    // ✅ ADDED: Loading state for API calls
    var isLoading by mutableStateOf(false)
        private set
    // ✅ ADDED: General error message
    var apiError by mutableStateOf<String?>(null)
        private set

    private val _googleSignInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: StateFlow<GoogleSignInState> = _googleSignInState.asStateFlow()

    fun onTabSelected(tab: AuthTab) {
        selectedTab = tab
        signUpPhoneError = null
        signInPhoneError = null
        apiError = null // ✅ Clear API error
        if (_googleSignInState.value is GoogleSignInState.Error) {
            resetGoogleSignInState()
        }
    }

    fun onSignUpPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signUpPhone = value
            apiError = null
            if (signUpPhoneError != null) {
                validateSignUpPhone()
            }
        }
    }

    fun onSignInPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signInPhone = value
            apiError = null
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

    // --- Click Handlers with API Validation ---

    // ✅ MODIFIED: Calls checkUser API
    fun onSignUpClicked(onNavigate: (String) -> Unit) {
        if (!validateSignUpPhone()) return

        viewModelScope.launch {
            isLoading = true
            apiError = null
            try {
                val request = CheckUserRequest(phoneNumber = "+91${signUpPhone}")
                val response = customApiService.checkUser(request)

                if (response.isSuccessful && response.body()?.userExists == true) {
                    // User already exists
                    apiError = "This phone number is already registered. Please Sign In."
                } else {
                    // User does not exist (or 404), proceed to Sign Up
                    onNavigate(signUpPhone)
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkUser for Sign Up failed", e)
                // Assume 404 (user not found) is an exception, which is good for sign up
                onNavigate(signUpPhone)
            } finally {
                isLoading = false
            }
        }
    }

    // ✅ MODIFIED: Calls checkUser API
    fun onSignInClicked(onNavigate: (String) -> Unit) {
        if (!validateSignInPhone()) return

        viewModelScope.launch {
            isLoading = true
            apiError = null
            try {
                val request = CheckUserRequest(phoneNumber = "+91${signInPhone}")
                val response = customApiService.checkUser(request)

                if (response.isSuccessful && response.body()?.userExists == true) {
                    // User exists, proceed to OTP
                    onNavigate(signInPhone)
                } else {
                    // User does not exist
                    apiError = "This phone number is not registered. Please Register."
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkUser for Sign In failed", e)
                apiError = "User not found. Please Register."
            } finally {
                isLoading = false
            }
        }
    }

    // --- Google Sign-In Logic (Commented out as per previous request) ---

    // fun onGoogleSignInClicked(activityContext: Context) { ... }
    // private suspend fun firebaseSignInWithGoogle(idToken: String) { ... }
    fun resetGoogleSignInState() {
        _googleSignInState.value = GoogleSignInState.Idle
    }
}