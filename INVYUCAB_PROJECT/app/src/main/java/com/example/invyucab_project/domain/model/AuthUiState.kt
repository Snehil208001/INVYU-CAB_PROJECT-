// This file contains UI state classes for the AuthScreen.
// These models are used by the AuthViewModel.
package com.example.invyucab_project.domain.model

import com.google.firebase.auth.FirebaseUser

// Represents the selected tab in the UI
enum class AuthTab {
    SIGN_UP,
    SIGN_IN
}

// Represents the state of the Google Sign-In flow
sealed class GoogleSignInState {
    object Idle : GoogleSignInState()
    object Loading : GoogleSignInState()
    data class Success(val user: FirebaseUser, val isNewUser: Boolean) : GoogleSignInState()
    data class Error(val message: String) : GoogleSignInState()
}