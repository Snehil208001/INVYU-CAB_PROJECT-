package com.example.invyucab_project.mainui.otpscreen.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.data.api.CustomApiService
import com.example.invyucab_project.data.models.UpdateUserStatusRequest
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val customApiService: CustomApiService, // ✅ INJECTED
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "OtpViewModel"

    // --- User Data from Navigation ---
    val fullPhoneNumber: String = savedStateHandle.get<String>("phone") ?: ""
    val email: String? = savedStateHandle.get<String>("email")
    val name: String? = savedStateHandle.get<String>("name")
    val gender: String? = savedStateHandle.get<String>("gender")
    val dob: String? = savedStateHandle.get<String>("dob")
    private val isSignUp: Boolean = savedStateHandle.get<Boolean>("isSignUp") ?: false

    // --- UI State ---
    var otp by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // --- Firebase Internal State ---
    private var verificationId: String? by mutableStateOf(null)
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    // --- State for Auto-Verification ---
    // This will hold the nav callbacks if auto-verify finishes
    private var pendingNavCallbacks: Pair<((String, String?, String?, String?, String?) -> Unit), (() -> Unit)>? = null

    init {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $id")
                isLoading = false
                verificationId = id
                error = null
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ", e)
                isLoading = false
                error = e.message ?: "Verification failed. Please try again."
            }

            // This is for auto-retrieval
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: Auto-retrieval success.")
                otp = credential.smsCode ?: "" // Pre-fill the OTP box
                isLoading = true

                // If the nav callbacks are ready, sign in automatically
                pendingNavCallbacks?.let { (onNavToRole, onNavToHome) ->
                    Log.d(TAG, "Auto-retrieving and navigating...")
                    signInWithCredential(credential, onNavToRole, onNavToHome)
                } ?: run {
                    // Otherwise, just pre-fill and wait for user to click
                    Log.d(TAG, "Auto-retrieval complete. Waiting for user to tap Verify.")
                    isLoading = false
                    error = "Auto-retrieval complete. Please tap Verify."
                }
            }
        }
    }

    fun onOtpChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            otp = value
            error = null
        }
    }

    fun sendOtp(activity: Activity) {
        if (fullPhoneNumber.isEmpty()) {
            error = "Phone number is missing."
            return
        }
        isLoading = true
        error = null
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$fullPhoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * ✅ RENAMED: This is the one and only verify function for the UI to call.
     */
    fun onVerifyClicked(
        onNavigateToRoleSelection: (phone: String, email: String?, name: String?, gender: String?, dob: String?) -> Unit,
        onNavigateToHome: () -> Unit
    ) {
        // Store the nav callbacks in case auto-verification is running
        pendingNavCallbacks = Pair(onNavigateToRoleSelection, onNavigateToHome)

        if (otp.length != 6) {
            error = "OTP must be 6 digits"
            return
        }
        val currentVerificationId = verificationId
        if (currentVerificationId == null) {
            error = "Verification process not started. Please try again."
            return
        }

        isLoading = true
        error = null
        val credential = PhoneAuthProvider.getCredential(currentVerificationId, otp)
        signInWithCredential(credential, onNavigateToRoleSelection, onNavigateToHome)
    }

    /**
     * ✅ RENAMED: This is the one and only helper
     */
    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        onNavigateToRoleSelection: (phone: String, email: String?, name: String?, gender: String?, dob: String?) -> Unit,
        onNavigateToHome: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Step 1: Verify OTP with Firebase
                auth.signInWithCredential(credential).await()
                Log.d(TAG, "Firebase sign-in successful.")

                // Step 2: Update status on our custom backend
                val statusRequest = UpdateUserStatusRequest(
                    phoneNumber = "+91$fullPhoneNumber",
                    status = "active",
                    email = email
                )
                customApiService.updateUserStatus(statusRequest)
                Log.d(TAG, "Custom API user status updated to active.")

                // Step 3: Navigate
                isLoading = false
                if (isSignUp) {
                    onNavigateToRoleSelection(fullPhoneNumber, email, name, gender, dob)
                } else {
                    onNavigateToHome()
                }

            } catch (e: Exception) {
                Log.e(TAG, "signInWithCredential failed: ", e)
                isLoading = false
                if (e is FirebaseException) {
                    error = "Verification failed. Please check the OTP."
                } else {
                    error = "Failed to update user status: ${e.message}"
                }
            }
        }
    }
}