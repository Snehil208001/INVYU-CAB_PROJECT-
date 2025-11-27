package com.example.invyucab_project.mainui.ridebookingscreen.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.models.RideBookingUiState
import com.example.invyucab_project.mainui.ridebookingscreen.viewmodel.RideBookingViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideBookingScreen(
    navController: NavController,
    rideId: String?,
    userPin: Int?,
    viewModel: RideBookingViewModel = hiltViewModel()
) {
    val uiState: RideBookingUiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ✅ Observe Navigation Events from Polling
    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { ride ->
            // Navigate to Booking Detail Screen when driver accepts
            val route = Screen.BookingDetailScreen.createRoute(
                driverName = ride.driverName ?: "Driver",
                vehicleModel = ride.model ?: "Vehicle",
                otp = ride.userPin?.toString() ?: "0000",
                rideId = ride.rideId,
                riderId = ride.riderId ?: 0,
                driverId = ride.driverId ?: 0,
                role = "rider",
                pickupLat = ride.pickupLatitude?.toDoubleOrNull() ?: 0.0,
                pickupLng = ride.pickupLongitude?.toDoubleOrNull() ?: 0.0,
                dropLat = ride.dropLatitude?.toDoubleOrNull() ?: 0.0,
                dropLng = ride.dropLongitude?.toDoubleOrNull() ?: 0.0
            )
            navController.navigate(route) {
                // Pop the searching screen so user can't go back to "Searching"
                popUpTo(Screen.RideBookingScreen.route) { inclusive = true }
            }
        }
    }

    val context = LocalContext.current
    val mapStyleOptions = remember {
        try {
            val json = context.resources.openRawResource(R.raw.map_style_retro)
                .bufferedReader()
                .use { it.readText() }
            MapStyleOptions(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ride Booking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = CabVeryLightMint
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
            } else {
                // 1. Map Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(LatLng(25.5941, 85.1376), 12f)
                    }

                    // Automatically move camera to fit route or markers
                    LaunchedEffect(uiState.pickupLocation, uiState.dropLocation, uiState.routePolyline) {
                        val pickup = uiState.pickupLocation
                        val drop = uiState.dropLocation
                        val polyline = uiState.routePolyline

                        val boundsBuilder = LatLngBounds.Builder()
                        var hasPoints = false

                        if (polyline.isNotEmpty()) {
                            polyline.forEach { boundsBuilder.include(it) }
                            hasPoints = true
                        } else {
                            pickup?.let {
                                boundsBuilder.include(it)
                                hasPoints = true
                            }
                            drop?.let {
                                boundsBuilder.include(it)
                                hasPoints = true
                            }
                        }

                        if (hasPoints) {
                            try {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            compassEnabled = false,
                            scrollGesturesEnabled = false
                        ),
                        properties = MapProperties(mapStyleOptions = mapStyleOptions)
                    ) {
                        val pickupIcon = remember(context) {
                            bitmapDescriptorFromVector(context, R.drawable.ic_pickup_marker)
                        }
                        val dropIcon = remember(context) {
                            bitmapDescriptorFromVector(context, R.drawable.ic_dropoff_marker)
                        }

                        // Draw Polyline
                        if (uiState.routePolyline.isNotEmpty()) {
                            Polyline(
                                points = uiState.routePolyline,
                                color = Color.Black,
                                width = 10f
                            )
                        }

                        // Draw Markers
                        uiState.pickupLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Pickup",
                                icon = pickupIcon
                            )
                        }

                        uiState.dropLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Drop",
                                icon = dropIcon
                            )
                        }
                    }
                }

                // 2. Ride Details Card
                RideDetailsCard(
                    pickup = uiState.pickupDescription,
                    drop = uiState.dropDescription
                )

                // 3. Driver/Searching Card (✅ Now passing PIN)
                SearchingCard(
                    isSearching = uiState.isSearchingForDriver,
                    userPin = userPin, // ✅ Passed the pin here
                    onCancel = { viewModel.onCancelRide() }
                )
            }
        }
    }
}

// Helper function for map icons
fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int
): BitmapDescriptor? {
    return try {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun RideDetailsCard(pickup: String, drop: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocationTimeline()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pickup,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    fontSize = 15.sp
                )
                Divider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )
                Text(
                    text = drop,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun SearchingCard(
    isSearching: Boolean,
    userPin: Int?, // ✅ Changed to accept Int?
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Searching for a Driver...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Display the PIN if available
            if (userPin != null && userPin != 0) { // Check for 0 if it's default
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = CabMintGreen.copy(alpha = 0.1f)),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Start PIN: $userPin",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CabMintGreen,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Give this PIN to driver",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder Animation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CabMintGreen)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.1f),
                    contentColor = Color.Red
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LocationTimeline() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(CabMintGreen.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CabMintGreen)
            )
        }
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        Canvas(
            modifier = Modifier
                .height(24.dp)
                .width(1.dp)
        ) {
            drawLine(
                color = Color.Gray,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 2f,
                pathEffect = pathEffect,
                alpha = 0.7f
            )
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}