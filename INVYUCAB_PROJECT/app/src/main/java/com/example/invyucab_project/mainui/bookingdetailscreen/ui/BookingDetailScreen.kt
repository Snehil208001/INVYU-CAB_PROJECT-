package com.example.invyucab_project.mainui.bookingdetailscreen.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.invyucab_project.R
import com.example.invyucab_project.core.common.Resource
import com.example.invyucab_project.mainui.bookingdetailscreen.viewmodel.BookingDetailViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookingDetailScreen(
    navController: NavController,
    rideId: Int = 1, // Pass the actual rideId from navigation arguments here
    viewModel: BookingDetailViewModel = hiltViewModel()
) {
    val rideState by viewModel.rideState.collectAsState()
    val routePolyline by viewModel.routePolyline.collectAsState()
    val context = LocalContext.current

    // Fetch data when screen loads
    LaunchedEffect(key1 = rideId) {
        viewModel.fetchOngoingRide(rideId)
    }

    // Default Camera Position
    val defaultLocation = LatLng(28.7041, 77.1025)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    // Retro Map Style
    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_retro)
        )
    }

    // Convert Vector XML to BitmapDescriptor to prevent crash
    val pickupIcon = remember(context) {
        bitmapDescriptorFromVector(context, R.drawable.ic_pickup_marker)
    }
    val dropoffIcon = remember(context) {
        bitmapDescriptorFromVector(context, R.drawable.ic_dropoff_marker)
    }

    Scaffold(
        content = {
            Box(modifier = Modifier.fillMaxSize()) {

                // --- 1. Map Section ---
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = false)
                ) {
                    // Draw Polyline
                    if (routePolyline.isNotEmpty()) {
                        Polyline(
                            points = routePolyline,
                            color = Color.Black,
                            width = 12f
                        )
                    }

                    if (rideState is Resource.Success) {
                        val response = (rideState as Resource.Success).data
                        val rideItem = response?.data?.firstOrNull()

                        if (rideItem != null) {
                            val pickupLat = rideItem.pickupLatitude?.toDoubleOrNull() ?: 28.7041
                            val pickupLng = rideItem.pickupLongitude?.toDoubleOrNull() ?: 77.1025
                            val dropLat = rideItem.dropLatitude?.toDoubleOrNull() ?: 28.7141
                            val dropLng = rideItem.dropLongitude?.toDoubleOrNull() ?: 77.1125

                            val pickupPos = LatLng(pickupLat, pickupLng)
                            val dropPos = LatLng(dropLat, dropLng)

                            // Pickup Marker
                            Marker(
                                state = MarkerState(position = pickupPos),
                                title = "Pickup",
                                icon = pickupIcon
                            )

                            // Dropoff Marker
                            Marker(
                                state = MarkerState(position = dropPos),
                                title = "Dropoff",
                                icon = dropoffIcon
                            )

                            // Center camera on pickup on first load
                            LaunchedEffect(pickupPos) {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(pickupPos, 15f)
                            }
                        }
                    }
                }

                // --- 2. Loading Indicator ---
                if (rideState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // --- 3. Booking Details Bottom Sheet ---
                if (rideState is Resource.Success) {
                    val response = (rideState as Resource.Success).data
                    val rideItem = response?.data?.firstOrNull()

                    if (rideItem != null) {
                        RideDetailsBottomCard(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(bottom = 16.dp)
                                .zIndex(1f),
                            driverName = rideItem.driverName ?: "Unknown Driver",
                            driverPhoto = rideItem.driverPhoto ?: "",
                            driverRating = rideItem.driverRating ?: "4.5",
                            vehicleNumber = rideItem.vehicleNumber ?: "N/A",
                            vehicleModel = rideItem.model ?: "Cab",
                            userPin = rideItem.userPin?.toString() ?: "----",
                            // ✅ Fetch pickup address from API (with fallback)
                            pickupAddress = rideItem.pickupAddress ?: "Address not available"
                        )
                    }
                } else if (rideState is Resource.Error) {
                    // Error Message
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = (rideState as Resource.Error).message
                                ?: "Error loading ride details",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun RideDetailsBottomCard(
    modifier: Modifier = Modifier,
    driverName: String,
    driverPhoto: String,
    driverRating: String,
    vehicleNumber: String,
    vehicleModel: String,
    userPin: String,
    pickupAddress: String
) {
    val scrollState = rememberScrollState()
    var replyText by remember { mutableStateOf("") }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {

            // Dummy Text for Pickup Time & Captain Distance
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pickup in 2 mins",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Captain 428 m away",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Driver Profile Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Driver Image
                AsyncImage(
                    model = driverPhoto,
                    contentDescription = "Driver Photo",
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Name & Rating
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700), // Gold
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = driverRating,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // Vehicle Number Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = vehicleNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Call Button
                IconButton(
                    onClick = { /* TODO: Handle Call */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF4CAF50), CircleShape) // Green
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call Driver",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Model & OTP Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vehicle Model",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = vehicleModel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "OTP (Start Ride)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = userPin,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Reply Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 14.sp),
                    decorationBox = { innerTextField ->
                        if (replyText.isEmpty()) {
                            Text(
                                text = "Message driver...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { /* TODO: Handle send message */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Pickup from and Trip details ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pickup from",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = pickupAddress,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            // Trip details row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Navigate to Trip Details */ }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Trip Details",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Trip details",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Helper Function for Vector Icons
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
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