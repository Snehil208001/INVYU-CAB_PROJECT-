package com.example.invyucab_project.mainui.authscreen.viewmodel

import android.content.Context
import android.util.Patterns // Import for email validation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull // Make sure this import is present
import javax.inject.Inject

enum class AuthTab {
    SIGN_UP,
    SIGN_IN
}

/**
 * Represents the state of the Google Sign-In flow.
 */
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    var selectedTab by mutableStateOf(AuthTab.SIGN_UP)
        private set

    var signUpEmail by mutableStateOf("")
        private set
    var signUpEmailError by mutableStateOf<String?>(null)
        private set

    var signUpPhone by mutableStateOf("")
        private set
    var signUpPhoneError by mutableStateOf<String?>(null)
        private set

    var signInPhone by mutableStateOf("")
        private set
    var signInPhoneError by mutableStateOf<String?>(null)
        private set

    // StateFlow to expose Google Sign-In state to the UI
    private val _googleSignInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: StateFlow<GoogleSignInState> = _googleSignInState.asStateFlow()

    fun onTabSelected(tab: AuthTab) {
        selectedTab = tab
        // Clear errors when switching tabs
        signUpEmailError = null
        signUpPhoneError = null
        signInPhoneError = null
        // Also reset Google sign in state if it was in an error state
        if (_googleSignInState.value is GoogleSignInState.Error) {
            resetGoogleSignInState()
        }
    }

    fun onSignUpEmailChange(value: String) {
        signUpEmail = value
        if (signUpEmailError != null) {
            validateSignUpEmail()
        }
    }

    fun onSignUpPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signUpPhone = value
            if (signUpPhoneError != null) {
                validateSignUpPhone()
            }
        }
    }

    fun onSignInPhoneChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 10) {
            signInPhone = value
            if (signInPhoneError != null) {
                validateSignInPhone()
            }
        }
    }

    // --- Validation Functions ---

    private fun validateSignUpEmail(): Boolean {
        if (signUpEmail.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(signUpEmail).matches()) {
            signUpEmailError = "Invalid email format"
            return false
        }
        signUpEmailError = null
        return true
    }

    private fun validateSignUpPhone(): Boolean {
        if (signUpPhone.length != 10) {
            signUpPhoneError = "Must be 10 digits"
            return false
        }
        signUpPhoneError = null
        return true
    }

    private fun validateSignInPhone(): Boolean {
        if (signInPhone.length != 10) {
            signInPhoneError = "Must be 10 digits"
            return false
        }
        signInPhoneError = null
        return true
    }

    // --- Click Handlers with Validation ---

    fun onSignUpClicked(onNavigate: (String) -> Unit) {
        val isEmailValid = validateSignUpEmail()
        val isPhoneValid = validateSignUpPhone()

        if (isEmailValid && isPhoneValid) {
            // TODO: Implement actual Sign Up API call and OTP sending
            onNavigate(signUpPhone)
        }
    }

    fun onSignInClicked(onNavigate: (String) -> Unit) {
        if (validateSignInPhone()) {
            // TODO: Implement actual Sign In API call and OTP sending
            onNavigate(signInPhone)
        }
    }

    // --- Google Sign-In Logic ---

    fun onGoogleSignInClicked() {
        // Get the Web Client ID from your google-services.json file
        // It's best practice to store this in res/values/strings.xml,
        // but for this example, we'll use the one from your file.
        // Make sure to add this ID to your strings.xml
        val serverClientId = "4006876917-onht2bdb8l3vjbvg8eranfceuapk8efc.apps.googleusercontent.com"

        // 1. Configure Google One Tap
        val googleIdTokenRequest = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        // 2. Build the Credential Manager Request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdTokenRequest)
            .build()

        // 3. Launch coroutine to get credential
        viewModelScope.launch {
            _googleSignInState.value = GoogleSignInState.Loading
            try {
                // Add a 10-second timeout for the credential manager
                val result: GetCredentialResponse? = withTimeoutOrNull(10000L) {
                    credentialManager.getCredential(
                        context = context,
                        request = request
                    )
                }

                // Check if result is null (which means it timed out)
                if (result == null) {
                    _googleSignInState.value = GoogleSignInState.Error(
                        "Google Sign-In timed out. Check emulator connection and ensure it has Google Play."
                    )
                    return@launch
                }

                // 5. Handle the result
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    // 6. Got the ID token, now sign in with Firebase
                    firebaseSignInWithGoogle(credential.idToken)
                } else {
                    _googleSignInState.value = GoogleSignInState.Error("Sign-In failed: Unexpected credential type.")
                }

            }
            catch (e: GetCredentialException) {
                // Handle credential-specific exceptions (e.g., user cancelled the One Tap flow)
                _googleSignInState.value = GoogleSignInState.Error(e.message ?: "Google Sign-In failed or was cancelled.")
            }
            catch (e: Exception) {
                // Handle other generic exceptions
                _googleSignInState.value = GoogleSignInState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    private suspend fun firebaseSignInWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

            if (user != null) {
                // 7. Success! Update the state
                _googleSignInState.value = GoogleSignInState.Success(user, isNewUser)
            } else {
                _googleSignInState.value = GoogleSignInState.Error("Firebase sign-in failed: User is null")
            }
        } catch (e: Exception) {
            _googleSignInState.value = GoogleSignInState.Error(e.message ?: "Firebase sign-in failed")
        }
    }

    /**
     * Resets the Google Sign-In state to Idle, e.g., after handling a Success or Error.
     */
    fun resetGoogleSignInState() {
        _googleSignInState.value = GoogleSignInState.Idle
    }
}