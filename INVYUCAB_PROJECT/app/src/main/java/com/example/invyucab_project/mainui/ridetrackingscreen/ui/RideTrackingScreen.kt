package com.example.invyucab_project.mainui.ridetrackingscreen.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen // ✅ Important Import
import com.example.invyucab_project.mainui.ridetrackingscreen.viewmodel.RideTrackingViewModel
import com.example.invyucab_project.ui.theme.CabLightGreen
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabPrimaryGreen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
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
    val context = LocalContext.current
    // Observe Success State
    val startRideSuccess by viewModel.startRideSuccess
    val snackbarHostState = remember { SnackbarHostState() }

    // Map Locations
    val pickupLocation = LatLng(pickupLat, pickupLng)
    val dropLocation = LatLng(dropLat, dropLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pickupLocation, 15f)
    }

    // --- Assets State ---
    var mapStyleOptions by remember { mutableStateOf<MapStyleOptions?>(null) }
    var pickupIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var dropIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // --- Initialization ---
    LaunchedEffect(Unit) {
        try {
            // Fix Crash
            MapsInitializer.initialize(context)
            pickupIcon = bitmapDescriptorFromDrawable(context, R.drawable.ic_pickup_marker)
            dropIcon = bitmapDescriptorFromDrawable(context, R.drawable.ic_dropoff_marker)

            val json = context.resources.openRawResource(R.raw.map_style_retro).bufferedReader().use { it.readText() }
            mapStyleOptions = MapStyleOptions(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- ✅ NAVIGATION LOGIC ---
    LaunchedEffect(startRideSuccess) {
        if (startRideSuccess) {
            // Navigate to RideInProgressScreen using the helper method in Screen.kt
            navController.navigate(
                Screen.RideInProgressScreen.createRoute(
                    rideId = rideId,
                    dropLat = dropLat,
                    dropLng = dropLng
                )
            ) {
                // Clear back stack so user can't go back to tracking screen
                popUpTo(Screen.RideTrackingScreen.route) { inclusive = true }
            }
        }
    }

    // --- Driver OTP Input State ---
    var otpInput by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is BaseViewModel.UiEvent.Navigate -> navController.navigate(event.route)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // 1. Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = mapStyleOptions,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = true)
            ) {
                // Draw Polyline if you have route points (not implemented in base VM uploaded)
                // ...

                pickupIcon?.let { icon ->
                    Marker(
                        state = MarkerState(position = pickupLocation),
                        title = "Pickup",
                        icon = icon
                    )
                }
                dropIcon?.let { icon ->
                    Marker(
                        state = MarkerState(position = dropLocation),
                        title = "Drop Location",
                        icon = icon
                    )
                }
            }

            // 2. Bottom Sheet / Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (role == "driver") "Enter Rider OTP" else "Your Ride PIN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (role == "driver") {
                        OtpInputField(otpText = otpInput, onOtpModified = { value, _ -> if (value.length <= 4) otpInput = value })
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { if (otpInput.length == 4) viewModel.startRide(rideId, riderId, driverId, otpInput) },
                            enabled = otpInput.length == 4,
                            colors = ButtonDefaults.buttonColors(containerColor = CabPrimaryGreen),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("START RIDE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Text(
                            text = otp,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = CabPrimaryGreen,
                            letterSpacing = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OtpInputField(otpText: String, onOtpModified: (String, Boolean) -> Unit) {
    BasicTextField(
        value = otpText,
        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) onOtpModified(it, it.length == 4) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(4) { index ->
                    val char = if (index < otpText.length) otpText[index].toString() else ""
                    val isFocused = otpText.length == index
                    Box(
                        modifier = Modifier.width(50.dp).height(56.dp)
                            .border(if (isFocused) 2.dp else 1.dp, if (isFocused) CabMintGreen else Color.Gray, RoundedCornerShape(12.dp))
                            .background(if (isFocused) CabLightGreen.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    )
}

fun bitmapDescriptorFromDrawable(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}