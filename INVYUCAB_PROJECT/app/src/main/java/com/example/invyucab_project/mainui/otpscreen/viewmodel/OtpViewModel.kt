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
import com.example.invyucab_project.data.models.AddVehicleRequest
import com.example.invyucab_project.data.models.CreateUserRequest
import com.example.invyucab_project.domain.usecase.ActivateUserUseCase
import com.example.invyucab_project.domain.usecase.AddVehicleUseCase
import com.example.invyucab_project.domain.usecase.CreateUserUseCase
import com.example.invyucab_project.domain.usecase.SaveUserStatusUseCase
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
    private val activateUserUseCase: ActivateUserUseCase,
    private val saveUserStatusUseCase: SaveUserStatusUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val addVehicleUseCase: AddVehicleUseCase, // ✅ INJECTED
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

    // Vehicle Details
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

                    // Navigate based on role
                    val route = when (role.lowercase()) {
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
            vehicleId = null, // This is null because we add vehicle in the next step
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
                    // User is created, save status
                    saveUserStatusUseCase.invoke("active")

                    // NOW, check role
                    if (finalRole == "driver") {
                        // User is a driver, now add their vehicle
                        val newDriverId = result.data?.userId
                        if (newDriverId == null) {
                            _isLoading.value = false
                            _apiError.value = "Created user but did not get a User ID."
                            return@onEach
                        }

                        // Check if all required vehicle details are present
                        if (vehicleNumber == null || vehicleModel == null || vehicleType == null || vehicleColor == null || vehicleCapacity == null) {
                            _isLoading.value = false
                            _apiError.value = "User created, but missing vehicle details."
                            Log.e(TAG, "Driver sign up failed: Missing vehicle details.")
                            return@onEach
                        }

                        val addVehicleRequest = AddVehicleRequest(
                            driverId = newDriverId,
                            vehicleNumber = vehicleNumber,
                            model = vehicleModel,
                            type = vehicleType,
                            color = vehicleColor,
                            capacity = vehicleCapacity
                        )

                        // Call the second API
                        callAddVehicleApi(addVehicleRequest)

                    } else {
                        // User is a Rider, so we are done
                        _isLoading.value = false
                        isAutoVerificationRunning = false
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

    private fun callAddVehicleApi(request: AddVehicleRequest) {
        addVehicleUseCase(request).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                }
                is Resource.Success -> {
                    // Vehicle added successfully
                    _isLoading.value = false
                    isAutoVerificationRunning = false
                    Log.d(TAG, "AddVehicle successful. Vehicle ID: ${result.data?.data}")

                    // Navigate to Driver Screen
                    sendEvent(UiEvent.Navigate(Screen.DriverScreen.route))
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    isAutoVerificationRunning = false
                    _apiError.value = "User created, but failed to add vehicle: ${result.message}"
                    Log.e(TAG, "Failed to add vehicle: ${result.message}")
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun formatDobForApi(dobString: String?): String? {
        if (dobString.isNullOrBlank()) return null
        return try {
            // This format must match the one in DriverDetailsScreen
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