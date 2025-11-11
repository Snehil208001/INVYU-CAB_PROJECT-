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
import com.example.invyucab_project.data.models.CreateUserRequest
import com.example.invyucab_project.domain.usecase.ActivateUserUseCase
import com.example.invyucab_project.domain.usecase.CreateUserUseCase
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val activateUserUseCase: ActivateUserUseCase,
    private val saveUserStatusUseCase: SaveUserStatusUseCase,
    private val createUserUseCase: CreateUserUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val TAG = "OtpViewModel"

    // --- User Data from Navigation ---
    val fullPhoneNumber: String = savedStateHandle.get<String>("phone") ?: ""
    private val isSignUp: Boolean = savedStateHandle.get<Boolean>("isSignUp") ?: false
    val role: String = savedStateHandle.get<String>("role") ?: "rider"

    // ❌ REMOVED email
    // val email: String? = decodeParam(savedStateHandle.get<String>("email"))
    val name: String? = decodeParam(savedStateHandle.get<String>("name"))
    val gender: String? = decodeParam(savedStateHandle.get<String>("gender"))
    val dob: String? = decodeParam(savedStateHandle.get<String>("dob"))
    val license: String? = decodeParam(savedStateHandle.get<String>("license"))
    val vehicle: String? = decodeParam(savedStateHandle.get<String>("vehicle"))
    val aadhaar: String? = decodeParam(savedStateHandle.get<String>("aadhaar"))


    // --- UI State ---
    var otp by mutableStateOf("")
        private set

    // --- Firebase Internal State ---
    private var verificationId: String? by mutableStateOf(null)
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    // --- State for Auto-Verification ---
    private var isAutoVerificationRunning = false

    init {
        Log.d(TAG, "OTP Screen loaded. Mode: ${if(isSignUp) "Sign Up" else "Sign In"}")
        // ❌ REMOVED email from log
        Log.d(TAG, "Data: $fullPhoneNumber, $role, $name, $dob, $gender, $license, $vehicle, $aadhaar")

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
                signInWithCredential(credential)
            }
        }
    }

    private fun decodeParam(param: String?): String? {
        if (param.isNullOrBlank()) return null
        return try {
            URLDecoder.decode(param, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            param
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

        if (isAutoVerificationRunning) return

        _isLoading.value = true
        _apiError.value = null
        val credential = PhoneAuthProvider.getCredential(currentVerificationId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                Log.d(TAG, "Firebase sign-in successful.")

                if (isSignUp) {
                    Log.d(TAG, "Sign-up flow: Creating user in backend...")
                    createUser()
                } else {
                    Log.d(TAG, "Sign-in flow: Updating user status to active.")
                    activateUser()
                }

            } catch (e: Exception) {
                Log.e(TAG, "signInWithCredential failed: ", e)
                _isLoading.value = false
                _apiError.value = "Verification failed. Please check the OTP."
                isAutoVerificationRunning = false
            }
        }
    }

    private fun activateUser() {
        // ✅ MODIFIED: Pass null for the email parameter
        activateUserUseCase.invoke(fullPhoneNumber, null).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                }
                is Resource.Success -> {
                    saveUserStatusUseCase.invoke("active")
                    Log.d(TAG, "User status 'active' saved to SharedPreferences.")

                    _isLoading.value = false
                    isAutoVerificationRunning = false

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

    private fun createUser() {
        val formattedDob = formatDobForApi(dob)
        val finalRole = role.lowercase()

        val request = CreateUserRequest(
            fullName = name ?: "User",
            phoneNumber = "+91$fullPhoneNumber",
            userRole = finalRole,
            profilePhotoUrl = null,
            gender = gender?.lowercase(),
            dob = formattedDob,
            licenseNumber = license,
            vehicleId = vehicle,
            rating = null,
            walletBalance = null,
            isVerified = true,
            status = "active"
        )

        createUserUseCase.invoke(request).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                }
                is Resource.Success -> {
                    saveUserStatusUseCase.invoke("active")
                    Log.d(TAG, "User status 'active' saved to SharedPreferences.")
                    _isLoading.value = false
                    isAutoVerificationRunning = false

                    val route = when (finalRole) {
                        "rider" -> Screen.HomeScreen.route
                        "driver" -> Screen.DriverScreen.route
                        "admin" -> Screen.AdminScreen.route
                        else -> Screen.HomeScreen.route
                    }

                    sendEvent(UiEvent.Navigate(route))
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _apiError.value = result.message
                    isAutoVerificationRunning = false
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun formatDobForApi(dobString: String?): String? {
        if (dobString.isNullOrBlank()) return null
        return try {
            val parser = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = parser.parse(dobString)
            formatter.format(date!!)
        } catch (e: Exception) {
            Log.e(TAG, "Could not parse date: $dobString", e)
            null
        }
    }
}