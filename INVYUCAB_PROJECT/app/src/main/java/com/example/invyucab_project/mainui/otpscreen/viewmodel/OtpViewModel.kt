package com.example.invyucab_project.mainui.otpscreen.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.usecase.ActivateUserUseCase
import com.example.invyucab_project.domain.usecase.SaveUserStatusUseCase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val activateUserUseCase: ActivateUserUseCase,
    private val saveUserStatusUseCase: SaveUserStatusUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

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

    // --- Firebase Internal State ---
    private var verificationId: String? by mutableStateOf(null)
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    // --- State for Auto-Verification ---
    private var isAutoVerificationRunning = false

    init {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $id")
                _isLoading.value = false
                verificationId = id
                _apiError.value = null
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ", e)
                _isLoading.value = false
                _apiError.value = e.message ?: "Verification failed. Please try again."
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: Auto-retrieval success.")
                otp = credential.smsCode ?: ""
                isAutoVerificationRunning = true
                signInWithCredential(credential) // Attempt auto-sign-in
            }
        }
    }

    fun onOtpChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            otp = value
            _apiError.value = null
        }
    }

    fun sendOtp(activity: Activity) {
        if (fullPhoneNumber.isEmpty()) {
            _apiError.value = "Phone number is missing."
            return
        }
        _isLoading.value = true
        _apiError.value = null
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$fullPhoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun onVerifyClicked() {
        if (otp.length != 6) {
            _apiError.value = "OTP must be 6 digits"
            return
        }
        val currentVerificationId = verificationId
        if (currentVerificationId == null) {
            _apiError.value = "Verification process not started. Please try again."
            return
        }

        // If auto-verification is already running, don't do it again
        if (isAutoVerificationRunning) return

        _isLoading.value = true
        _apiError.value = null
        val credential = PhoneAuthProvider.getCredential(currentVerificationId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                // Step 1: Verify OTP with Firebase
                auth.signInWithCredential(credential).await()
                Log.d(TAG, "Firebase sign-in successful.")

                // Step 2: Navigate OR Update Status
                if (isSignUp) {
                    // THIS IS SIGN-UP
                    _isLoading.value = false
                    Log.d(TAG, "Sign-up flow: Navigating to Role Selection.")
                    sendEvent(UiEvent.Navigate(
                        Screen.RoleSelectionScreen.createRoute(
                            phone = fullPhoneNumber,
                            email = email,
                            name = name,
                            gender = gender,
                            dob = dob
                        )
                    ))
                } else {
                    // THIS IS SIGN-IN
                    Log.d(TAG, "Sign-in flow: Updating user status to active.")
                    activateUser()
                }

            } catch (e: Exception) {
                Log.e(TAG, "signInWithCredential failed: ", e)
                _isLoading.value = false
                _apiError.value = "Verification failed. Please check the OTP."
                isAutoVerificationRunning = false // Reset auto-verify flag on failure
            }
        }
    }

    private fun activateUser() {
        activateUserUseCase.invoke(fullPhoneNumber, email).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                }
                is Resource.Success -> {
                    // Backend API call successful, now save to local prefs
                    saveUserStatusUseCase.invoke("active")
                    Log.d(TAG, "User status 'active' saved to SharedPreferences.")

                    _isLoading.value = false
                    isAutoVerificationRunning = false

                    // ✅✅✅ FIX: Navigate to HomeScreen after success ✅✅✅
                    sendEvent(UiEvent.Navigate(Screen.HomeScreen.route))
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _apiError.value = result.message
                    isAutoVerificationRunning = false
                }
            }
        }.launchIn(viewModelScope)
    }
}