package com.example.invyucab_project.mainui.ridetrackingscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.mainui.ridetrackingscreen.viewmodel.RideTrackingViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun RideTrackingScreen(
    navController: NavController,
    rideId: Int,
    riderId: Int,
    driverId: Int,
    role: String,
    pickupLat: Double,
    pickupLng: Double,
    dropLat: Double,
    dropLng: Double,
    otp: String,
    viewModel: RideTrackingViewModel = hiltViewModel()
) {
    val startRideSuccess by viewModel.startRideSuccess
    val snackbarHostState = remember { SnackbarHostState() }

    // Map State
    val pickupLocation = LatLng(pickupLat, pickupLng)
    val dropLocation = LatLng(dropLat, dropLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pickupLocation, 15f)
    }

    // Driver OTP Input State
    var otpInput by remember { mutableStateOf("") }

    // Event Handling
    LaunchedEffect(true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(startRideSuccess) {
        if (startRideSuccess) {
            // Handle success state (e.g., navigate to "In Ride" mode)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // --- Google Map ---
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = pickupLocation),
                    title = "Pickup Location",
                    snippet = "Wait here"
                )
                Marker(
                    state = MarkerState(position = dropLocation),
                    title = "Drop Location"
                )
            }

            // --- Bottom Sheet / Overlay ---
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (role == "driver") "Enter Rider PIN" else "Share PIN with Driver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (role == "driver") {
                        // --- Driver Side: Input OTP ---
                        OtpInputField(
                            otpText = otpInput,
                            onOtpModified = { value, _ ->
                                if (value.length <= 4) otpInput = value
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (otpInput.length == 4) {
                                    viewModel.startRide(rideId, riderId, driverId, otpInput)
                                }
                            },
                            enabled = otpInput.length == 4,
                            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Start Ride", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                    } else {
                        // --- Rider Side: Display OTP ---
                        Text(
                            text = otp,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = CabMintGreen,
                            letterSpacing = 8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Provide this PIN to your driver to start the ride.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpText: String,
    onOtpModified: (String, Boolean) -> Unit
) {
    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.length <= 4) {
                onOtpModified(it, it.length == 4)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(4) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    val isFocused = otpText.length == index
                    Text(
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) CabMintGreen else Color.Gray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(top = 10.dp),
                        text = char,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    )
}