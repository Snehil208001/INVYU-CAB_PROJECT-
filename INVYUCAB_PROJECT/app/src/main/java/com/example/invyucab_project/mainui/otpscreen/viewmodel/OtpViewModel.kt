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
// ✅ --- START OF FIX: IMPORT ADDED ---
import com.example.invyucab_project.data.preferences.UserPreferencesRepository
// ✅ --- END OF FIX ---
import com.example.invyucab_project.domain.usecase.CreateUserUseCase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val createUserUseCase: CreateUserUseCase,
    // ✅ --- START OF FIX: REPOSITORY INJECTED ---
    private val userPreferencesRepository: UserPreferencesRepository,
    // ✅ --- END OF FIX ---
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val TAG = "OtpViewModel"

    // --- User Data from Navigation ---
    val fullPhoneNumber: String = savedStateHandle.get<String>("phone") ?: ""
    private val isSignUp: Boolean = savedStateHandle.get<Boolean>("isSignUp") ?: false
    val role: String = savedStateHandle.get<String>("role") ?: "rider"

    // Personal details
    val name: String? = decodeParam(savedStateHandle.get<String>("name"))
    val gender: String? = decodeParam(savedStateHandle.get<String>("gender"))
    val dob: String? = decodeParam(savedStateHandle.get<String>("dob"))

    // Driver details
    val license: String? = decodeParam(savedStateHandle.get<String>("license"))
    val aadhaar: String? = decodeParam(savedStateHandle.get<String>("aadhaar"))

    // Vehicle Details (still received, but not used for AddVehicle)
    private val vehicleNumber: String? = decodeParam(savedStateHandle.get<String>("vehicleNumber"))
    private val vehicleModel: String? = decodeParam(savedStateHandle.get<String>("vehicleModel"))
    private val vehicleType: String? = decodeParam(savedStateHandle.get<String>("vehicleType"))
    private val vehicleColor: String? = decodeParam(savedStateHandle.get<String>("vehicleColor"))
    private val vehicleCapacity: String? = decodeParam(savedStateHandle.get<String>("vehicleCapacity"))


    // --- UI State ---
    var otp by mutableStateOf("")
        private set
    var resendTimer by mutableStateOf(60)
    var canResend by mutableStateOf(false)

    // --- Firebase Internal State ---
    private var verificationId: String? by mutableStateOf(null)
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var isAutoVerificationRunning = false

    init {
        Log.d(TAG, "OTP Screen loaded. Mode: ${if (isSignUp) "Sign Up" else "Sign In"}")
        Log.d(TAG, "Data: $fullPhoneNumber, $role, $name")
        Log.d(TAG, "Vehicle Data: $vehicleNumber, $vehicleType")

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
            .setPhoneNumber("+91$fullPhoneNumber") // Make sure it's the full number with country code
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        startResendTimer()
    }

    fun resendOtp(activity: Activity) {
        if (canResend) {
            sendOtp(activity)
        }
    }

    private fun startResendTimer() {
        canResend = false
        resendTimer = 60
        viewModelScope.launch {
            while (resendTimer > 0) {
                delay(1000)
                resendTimer--
            }
            canResend = true
        }
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
                    Log.d(TAG, "Sign-up flow: Handling sign up...")
                    handleSignUp()
                } else {
                    Log.d(TAG, "Sign-in flow: Navigating based on role.")
                    // Sign-in flow (no use cases, just navigate)
                    _isLoading.value = false
                    isAutoVerificationRunning = false

                    // ✅ --- START OF FIX: SAVE ROLE AND STATUS ON SIGN-IN ---
                    userPreferencesRepository.saveUserRole(role)
                    userPreferencesRepository.saveUserStatus("active")
                    Log.d(TAG, "Sign-in: Saved role '$role' and status 'active'.")
                    // ✅ --- END OF FIX ---

                    val route = when (role.lowercase()) {
                        "driver" -> Screen.DriverScreen.route
                        "admin" -> Screen.AdminScreen.route
                        else -> Screen.HomeScreen.route
                    }
                    sendEvent(UiEvent.Navigate(route))
                }

            } catch (e: Exception) {
                Log.e(TAG, "signInWithCredential failed: ", e)
                _isLoading.value = false
                _apiError.value = "Verification failed. Please check the OTP."
                isAutoVerificationRunning = false
            }
        }
    }

    private fun handleSignUp() {
        val formattedDob = formatDobForApi(dob)
        val finalRole = role.lowercase()

        // This request is used for BOTH Rider and Driver
        val createUserRequest = CreateUserRequest(
            fullName = name ?: "User",
            phoneNumber = "+91$fullPhoneNumber",
            userRole = finalRole,
            profilePhotoUrl = null,
            gender = gender?.lowercase(),
            dob = formattedDob,
            licenseNumber = license,
            vehicleId = null, // Vehicle logic removed from this viewmodel
            rating = null,
            walletBalance = null,
            isVerified = true,
            status = "active"
        )

        // Call createUser for ALL signups
        createUserUseCase.invoke(createUserRequest).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                }
                is Resource.Success -> {
                    Log.d(TAG, "CreateUser successful. User ID: ${result.data?.userId}")

                    // ✅ --- START OF FIX: SAVE THE USER ID, ROLE, and STATUS ---
                    val newUserId = result.data?.userId
                    if (newUserId != null) {
                        userPreferencesRepository.saveUserId(newUserId)
                        userPreferencesRepository.saveUserRole(finalRole)
                        userPreferencesRepository.saveUserStatus("active")
                        Log.d(TAG, "Sign-up: Saved User ID $newUserId, Role '$finalRole', and Status 'active'.")
                    } else {
                        Log.e(TAG, "CreateUser succeeded but userId was null in response.")
                    }
                    // ✅ --- END OF FIX ---

                    // User is created.
                    _isLoading.value = false
                    isAutoVerificationRunning = false

                    // NOW, check role and navigate
                    if (finalRole == "driver") {
                        // User is a Driver, navigate to DriverScreen
                        sendEvent(UiEvent.Navigate(Screen.DriverScreen.route))
                    } else {
                        // User is a Rider, so we are done
                        sendEvent(UiEvent.Navigate(Screen.HomeScreen.route))
                    }
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
            // ✅ --- START OF FIX: CHANGED PARSER FORMAT ---
            // The log "Could not parse date: 2025-11-17" shows the date is in yyyy-MM-dd format
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            // ✅ --- END OF FIX ---
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = parser.parse(dobString)
            formatter.format(date!!)
        } catch (e: Exception) {
            Log.e(TAG, "Could not parse date: $dobString", e)
            null
        }
    }
}