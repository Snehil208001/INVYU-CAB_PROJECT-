package com.example.invyucab_project.mainui.rideinprogressscreen.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.invyucab_project.R

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
import kotlinx.coroutines.delay

@Composable
fun RideInProgressScreen(
    navController: NavController,
    rideId: Int,
    dropLat: Double,
    dropLng: Double,
    otp: String,
    // Phone number of the other party (Rider or Driver) - fallback
    targetPhone: String? = null,
    viewModel: RideInProgressViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val rideState by viewModel.rideState.collectAsState()

    // --- Helper Function to Open Google Maps Navigation ---
    fun openGoogleMaps() {
        val gmmIntentUri = Uri.parse("google.navigation:q=$dropLat,$dropLng&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    // Automatically Open Maps on Screen Entry
    LaunchedEffect(Unit) {
        openGoogleMaps()
    }

    // Poll the server to get fresh ride details (specifically rider phone number)
    LaunchedEffect(key1 = rideId) {
        while (true) {
            viewModel.fetchOngoingRide(rideId)
            delay(4000)
        }
    }

    // Map State
    val dropLocation = LatLng(dropLat, dropLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(dropLocation, 16f)
    }

    // Load Retro Map Style
    val mapStyleOptions = remember {
        try {
            val json = context.resources.openRawResource(R.raw.map_style_retro).bufferedReader().use { it.readText() }
            MapStyleOptions(json)
        } catch (e: Exception) {
            null
        }
    }

    // Initialize Map
    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    // Handle API Success
    val updateState by viewModel.updateStatus.collectAsState()
    LaunchedEffect(updateState) {
        updateState?.onSuccess {
            navController.navigate(Screen.DriverScreen.route) {
                popUpTo(Screen.HomeScreen.route) { inclusive = false }
            }
        }
        updateState?.onFailure {
            // Handle error if needed
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Background Map
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

            // 2. Top Status Pill (Showing OTP and Clickable)
            Surface(
                onClick = { openGoogleMaps() },
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
                        // Displaying the OTP here
                        text = "PIN: $otp • NAVIGATE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // 3. Bottom Control Panel
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
                                text = "Heading to Destination",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Follow map to drop location",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        // Action Buttons
                        Row {
                            IconButton(
                                onClick = {
                                    // Use the phone number from API if available, else fallback
                                    var phoneToCall = targetPhone
                                    if (rideState is Resource.Success) {
                                        // ✅ This line will now compile correctly
                                        val fetchedPhone = (rideState as Resource.Success).data?.data?.firstOrNull()?.riderMobileNumber
                                        if (!fetchedPhone.isNullOrEmpty()) {
                                            phoneToCall = fetchedPhone
                                        }
                                    }

                                    if (!phoneToCall.isNullOrEmpty()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL)
                                            intent.data = Uri.parse("tel:$phoneToCall")
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Unable to make call", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Number not available", Toast.LENGTH_SHORT).show()
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
                                onClick = { },
                                modifier = Modifier
                                    .background(Color(0xFFFFECEC), CircleShape)
                                    .size(45.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = "SOS", tint = Color.Red)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Cancel Button
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
                }
            }
        }
    }
}