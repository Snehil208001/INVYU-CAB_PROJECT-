package com.example.invyucab_project.mainui.ridetrackingscreen.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.invyucab_project.R

import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.ridetrackingscreen.viewmodel.RideTrackingViewModel
import com.example.invyucab_project.ui.theme.CabLightGreen
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabPrimaryGreen
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
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
    driverPhone: String? = null,
    riderPhone: String? = null,
    viewModel: RideTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val startRideSuccess by viewModel.startRideSuccess

    val routePoints by viewModel.routePolyline
    // ✅ ADDED: Observe fetched rider phone from ViewModel
    val fetchedRiderPhone by viewModel.riderPhone
    // ✅ ADDED: Observe ride status
    val rideStatus by viewModel.rideStatus

    val snackbarHostState = remember { SnackbarHostState() }

    val pickupLocation = LatLng(pickupLat, pickupLng)
    val dropLocation = LatLng(dropLat, dropLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pickupLocation, 15.5f)
    }

    var mapStyleOptions by remember { mutableStateOf<MapStyleOptions?>(null) }
    var pickupIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var dropIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // --- Driver OTP Input State ---
    var otpInput by remember { mutableStateOf("") }

    // ✅ ADDED: Fetch Rider Details and Start Monitoring Ride Status
    LaunchedEffect(riderId) {
        viewModel.fetchRiderDetails(riderId)
        viewModel.monitorRideStatus(rideId) // Start polling
    }

    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
            pickupIcon = bitmapDescriptorFromDrawable(context, R.drawable.ic_pickup_marker)
            dropIcon = bitmapDescriptorFromDrawable(context, R.drawable.ic_dropoff_marker)
            val json = context.resources.openRawResource(R.raw.map_style_retro).bufferedReader().use { it.readText() }
            mapStyleOptions = MapStyleOptions(json)
            viewModel.fetchRoute(pickupLat, pickupLng, dropLat, dropLng)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ✅ ADDED: Handle Ride Cancellation
    LaunchedEffect(rideStatus) {
        if (rideStatus == "cancelled") {
            Toast.makeText(context, "Ride was cancelled.", Toast.LENGTH_LONG).show()
            if (role == "driver") {
                // Navigate back to Driver Home Screen
                navController.navigate(Screen.DriverScreen.route) {
                    popUpTo(Screen.DriverScreen.route) { inclusive = true }
                }
            } else {
                // Navigate back to Rider Home Screen
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.HomeScreen.route) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(startRideSuccess) {
        if (startRideSuccess) {
            // Determines who to call on the next screen
            // If I am driver, target is rider (try arg first, then fetched)
            // If I am rider, target is driver
            val targetPhoneForNextScreen = if (role == "driver") (riderPhone ?: fetchedRiderPhone) else driverPhone

            navController.navigate(
                Screen.RideInProgressScreen.createRoute(
                    rideId = rideId,
                    dropLat = dropLat,
                    dropLng = dropLng,
                    otp = otpInput,
                    targetPhone = targetPhoneForNextScreen
                )
            ) {
                popUpTo(Screen.RideTrackingScreen.route) { inclusive = true }
            }
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapStyleOptions = mapStyleOptions,
                        isMyLocationEnabled = false
                    ),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = true)
                ) {
                    if (routePoints.isNotEmpty()) {
                        Polyline(points = routePoints, color = CabPrimaryGreen, width = 12f)
                    }
                    pickupIcon?.let { icon -> Marker(state = MarkerState(position = pickupLocation), title = "Pickup", icon = icon) }
                    dropIcon?.let { icon -> Marker(state = MarkerState(position = dropLocation), title = "Drop", icon = icon) }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(24.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (role == "driver") {
                        DriverView(
                            otpInput = otpInput,
                            onOtpChange = { otpInput = it },
                            onStartRide = { viewModel.startRide(rideId, riderId, driverId, otpInput) }
                        )
                    } else {
                        RiderView(
                            otp = otp,
                            context = context,
                            onCallDriver = {
                                if (!driverPhone.isNullOrEmpty()) {
                                    // ✅ FIXED: Removed viewModel.initiateCall(driverPhone)
                                    // Added direct Intent call
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:$driverPhone")
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Driver number not available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiderView(otp: String, context: Context, onCallDriver: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = CabMintGreen.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(CabPrimaryGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Driver is arriving soon", style = MaterialTheme.typography.labelMedium, color = CabPrimaryGreen, fontWeight = FontWeight.Bold)
            }
        }

        Text("Your Ride PIN", style = MaterialTheme.typography.titleMedium, color = Color.Gray, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(otp, style = MaterialTheme.typography.displayLarge.copy(fontSize = 60.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp), color = Color.Black)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Ride PIN", otp)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.background(Color.LightGray.copy(alpha = 0.2f), CircleShape).size(44.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Share this code with the driver to start the ride.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCallDriver,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call Driver", color = Color.Black)
            }
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Safety", color = Color.White)
            }
        }
    }
}

@Composable
fun DriverView(otpInput: String, onOtpChange: (String) -> Unit, onStartRide: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Enter Rider's PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Ask the rider for the 4-digit start code", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        OtpInputField(otpText = otpInput, onOtpModified = { value, _ -> if (value.length <= 4) onOtpChange(value) })

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartRide,
            enabled = otpInput.length == 4,
            colors = ButtonDefaults.buttonColors(containerColor = CabPrimaryGreen, disabledContainerColor = Color.LightGray),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text("START RIDE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (otpInput.length == 4) Color.White else Color.DarkGray)
        }
    }
}

@Composable
fun OtpInputField(otpText: String, onOtpModified: (String, Boolean) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                onOtpModified(it, it.length == 4)
                if (it.length == 4) keyboardController?.hide()
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    val char = if (index < otpText.length) otpText[index].toString() else ""
                    val isFocused = otpText.length == index
                    val borderColor = if (isFocused) CabPrimaryGreen else Color.LightGray
                    val backgroundColor = if (isFocused) CabLightGreen.copy(alpha = 0.1f) else Color.Transparent
                    Box(
                        modifier = Modifier.width(60.dp).height(64.dp).border(if (isFocused) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)).background(backgroundColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
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