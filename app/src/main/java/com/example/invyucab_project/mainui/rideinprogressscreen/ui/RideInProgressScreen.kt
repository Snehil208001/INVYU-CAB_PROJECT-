package com.example.invyucab_project.mainui.rideinprogressscreen.ui

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.rideinprogressscreen.viewmodel.RideInProgressViewModel
import com.example.invyucab_project.ui.theme.CabLightGreen
import com.example.invyucab_project.ui.theme.CabPrimaryGreen
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

@Composable
fun RideInProgressScreen(
    navController: NavController,
    rideId: Int,
    dropLat: Double,
    dropLng: Double,
    role: String,
    otp: String,
    targetPhone: String? = null,
    viewModel: RideInProgressViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val rideState by viewModel.rideState.collectAsState()
    val scope = rememberCoroutineScope()

    var currentRiderPhone by remember { mutableStateOf<String?>(targetPhone) }
    var isRideCompleted by remember { mutableStateOf(false) }

    // Helper function to get address from LatLng
    suspend fun getAddress(lat: Double?, lng: Double?): String? {
        if (lat == null || lng == null) return null
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun navigateToBill(fare: String, pickup: String?, drop: String?, pLat: Double?, pLng: Double?, dLat: Double?, dLng: Double?) {
        // Prevent double navigation
        if (isRideCompleted) return
        isRideCompleted = true

        scope.launch {
            // Check if addresses are missing, if so, geocode them
            var finalPickup = pickup
            var finalDrop = drop

            if (finalPickup.isNullOrEmpty() && pLat != null && pLng != null) {
                finalPickup = getAddress(pLat, pLng)
            }
            if (finalDrop.isNullOrEmpty() && dLat != null && dLng != null) {
                finalDrop = getAddress(dLat, dLng)
            }

            // Fallback strings
            finalPickup = finalPickup ?: "Pickup Location"
            finalDrop = finalDrop ?: "Drop Location"

            Log.d("RideInProgress", "Navigating to Bill: Fare=$fare, Pickup=$finalPickup, Drop=$finalDrop")

            // Encode to ensure safe navigation URL
            val encodedPickup = URLEncoder.encode(finalPickup, StandardCharsets.UTF_8.toString())
            val encodedDrop = URLEncoder.encode(finalDrop, StandardCharsets.UTF_8.toString())

            navController.navigate(
                "bill_screen/$fare/$role?pickupAddress=$encodedPickup&dropAddress=$encodedDrop"
            ) {
                popUpTo(Screen.HomeScreen.route) { inclusive = false }
            }
        }
    }

    // Polling Logic - Stops when isRideCompleted is true
    LaunchedEffect(key1 = rideId, key2 = isRideCompleted) {
        while (!isRideCompleted) {
            viewModel.fetchOngoingRide(rideId)
            delay(4000)
        }
    }

    // Observe Status Changes
    LaunchedEffect(rideState) {
        if (rideState is Resource.Success) {
            val response = (rideState as Resource.Success).data
            val rideData = response?.data?.firstOrNull()

            if (rideData != null) {
                // Update phone if available
                val apiPhone = rideData.riderMobileNumber ?: rideData.driverPhone
                if (!apiPhone.isNullOrEmpty()) {
                    currentRiderPhone = apiPhone
                }

                // Handle Completed Status
                if (rideData.status.equals("completed", ignoreCase = true)) {
                    val fare = rideData.estimatedPrice ?: "0.0"

                    val pLat = rideData.pickupLatitude?.toDoubleOrNull()
                    val pLng = rideData.pickupLongitude?.toDoubleOrNull()
                    val dLat = rideData.dropLatitude?.toDoubleOrNull()
                    val dLng = rideData.dropLongitude?.toDoubleOrNull()

                    navigateToBill(
                        fare = fare,
                        pickup = rideData.pickupAddress,
                        drop = rideData.dropAddress,
                        pLat = pLat, pLng = pLng, dLat = dLat, dLng = dLng
                    )
                }

                // Handle Cancelled Status
                if (rideData.status.equals("cancelled", ignoreCase = true)) {
                    isRideCompleted = true // Stop polling
                    Toast.makeText(context, "Ride cancelled", Toast.LENGTH_LONG).show()
                    val targetScreen = if (role == "driver") Screen.DriverScreen.route else Screen.HomeScreen.route
                    navController.navigate(targetScreen) {
                        popUpTo(targetScreen) { inclusive = true }
                    }
                }
            }
        } else if (rideState is Resource.Error) {
            Log.e("RideInProgress", "Error: ${(rideState as Resource.Error).message}")
        }
    }

    val dropLocation = LatLng(dropLat, dropLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(dropLocation, 16f)
    }

    // Map Styling
    val mapStyleOptions = remember {
        try {
            val json = context.resources.openRawResource(R.raw.map_style_retro).bufferedReader().use { it.readText() }
            MapStyleOptions(json)
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
        } catch(e: Exception) {
            e.printStackTrace()
        }

        if (role == "driver") {
            val gmmIntentUri = Uri.parse("google.navigation:q=$dropLat,$dropLng&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            try {
                context.startActivity(mapIntent)
            } catch (e: Exception) {
                // Ignore if maps app not installed
            }
        }
    }

    // Handle Manual Driver Completion (Button Click)
    val updateState by viewModel.updateStatus.collectAsState()
    LaunchedEffect(updateState) {
        updateState?.onSuccess {
            // When driver clicks complete, immediately grab data from current state
            val data = (rideState as? Resource.Success)?.data?.data?.firstOrNull()
            val fare = data?.estimatedPrice ?: "0.0"

            // Get coordinates from current data (or fallback to passed args if data is null)
            val pLat = data?.pickupLatitude?.toDoubleOrNull()
            val pLng = data?.pickupLongitude?.toDoubleOrNull()
            // If data is null, we can try using dropLat/dropLng passed to composable as fallback for drop
            val dLat = data?.dropLatitude?.toDoubleOrNull() ?: dropLat
            val dLng = data?.dropLongitude?.toDoubleOrNull() ?: dropLng

            navigateToBill(
                fare = fare,
                pickup = data?.pickupAddress,
                drop = data?.dropAddress,
                pLat = pLat, pLng = pLng, dLat = dLat, dLng = dLng
            )
        }
        updateState?.onFailure {
            Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = mapStyleOptions,
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = dropLocation),
                    title = "Drop Location",
                    snippet = "Passenger Destination"
                )
            }

            // Top Navigation Pill
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp)
                    .clip(RoundedCornerShape(50)),
                color = CabPrimaryGreen,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PIN: $otp • NAVIGATE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Bottom Control Panel
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (role == "driver") "Heading to Destination" else "Enjoy your ride",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (role == "driver") "Follow map to drop location" else "You are on the way",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    val phoneToCall = currentRiderPhone
                                    if (!phoneToCall.isNullOrEmpty()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL)
                                            intent.data = Uri.parse("tel:$phoneToCall")
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .background(CabLightGreen, CircleShape)
                                    .size(45.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = CabPrimaryGreen)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "SOS triggered", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .background(Color(0xFFFFECEC), CircleShape)
                                    .size(45.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = "SOS", tint = Color.Red)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (role == "driver") {
                        OutlinedButton(
                            onClick = { viewModel.updateRideStatus(rideId, "cancelled") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text(
                                text = "CANCEL RIDE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.updateRideStatus(rideId, "completed") },
                            colors = ButtonDefaults.buttonColors(containerColor = CabPrimaryGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text(
                                text = "COMPLETE RIDE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = "Sit back and relax!",
                            color = CabPrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}