package com.example.invyucab_project.mainui.bookingdetailscreen.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.invyucab_project.core.navigations.Screen
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookingDetailScreen(
    navController: NavController,
    rideId: Int = 1,
    viewModel: BookingDetailViewModel = hiltViewModel()
) {
    val rideState by viewModel.rideState.collectAsState()
    val routePolyline by viewModel.routePolyline.collectAsState()
    val cancelState by viewModel.cancelRideState.collectAsState()
    val context = LocalContext.current

    var showTripDetailsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // ✅ UPDATED: Poll the server every 4 seconds to check for updates
    LaunchedEffect(key1 = rideId) {
        while (true) {
            viewModel.fetchOngoingRide(rideId)
            delay(4000) // Wait 4 seconds before fetching again
        }
    }

    // ✅ ADDED: Listen for Navigation Events (Driver Cancelled -> Search, Rider Cancelled -> Home)
    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is BookingDetailViewModel.BookingNavigationEvent.NavigateHome -> {
                    Toast.makeText(context, "Ride Ended", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                }
                is BookingDetailViewModel.BookingNavigationEvent.NavigateToSearching -> {
                    Toast.makeText(context, "Driver Cancelled. Searching new ride...", Toast.LENGTH_LONG).show()
                    val route = Screen.RideBookingScreen.createRoute(
                        rideId = event.rideId,
                        // ✅ FIX: Removed .toFloat() because createRoute expects Double
                        pickupLat = event.pickupLat,
                        pickupLng = event.pickupLng,
                        dropLat = event.dropLat,
                        dropLng = event.dropLng,
                        pickupAddress = event.pickupAddress,
                        dropAddress = event.dropAddress,
                        dropPlaceId = event.dropPlaceId,
                        userPin = event.userPin
                    )
                    navController.navigate(route) {
                        popUpTo(Screen.BookingDetailScreen.route) { inclusive = true }
                    }
                }
            }
        }
    }

    // ✅ Listen for Rider Cancellation Success and Navigate
    LaunchedEffect(cancelState) {
        if (cancelState is Resource.Success) {
            Toast.makeText(context, "Ride Cancelled Successfully", Toast.LENGTH_SHORT).show()
            // Navigate to Home Screen and remove backstack
            navController.navigate(Screen.HomeScreen.route) {
                popUpTo(Screen.HomeScreen.route) { inclusive = true }
            }
        } else if (cancelState is Resource.Error) {
            Toast.makeText(context, (cancelState as Resource.Error).message ?: "Cancellation Failed", Toast.LENGTH_LONG).show()
        }
    }

    val defaultLocation = LatLng(28.7041, 77.1025)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_retro)
        )
    }

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

                            Marker(
                                state = MarkerState(position = pickupPos),
                                title = "Pickup",
                                icon = pickupIcon
                            )

                            Marker(
                                state = MarkerState(position = dropPos),
                                title = "Dropoff",
                                icon = dropoffIcon
                            )

                            LaunchedEffect(pickupPos) {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(pickupPos, 15f)
                            }
                        }
                    }
                }

                // --- 2. Loading Indicator ---
                if (rideState is Resource.Loading || cancelState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // --- 3. Booking Details Bottom Sheet (Main Card) ---
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
                            pickupAddress = rideItem.pickupAddress ?: "Address not available",
                            onTripDetailsClick = { showTripDetailsSheet = true },
                            // ✅ ADDED: Calling Logic
                            onCallClick = {
                                val phone = rideItem.driverPhone
                                if (!phone.isNullOrEmpty()) {
                                    Toast.makeText(context, "Calling Driver...", Toast.LENGTH_SHORT).show()
                                    viewModel.initiateCall(phone)
                                } else {
                                    Toast.makeText(context, "Driver number not available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // --- 4. Trip Details Bottom Sheet Overlay ---
                        if (showTripDetailsSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showTripDetailsSheet = false },
                                sheetState = sheetState,
                                containerColor = Color.White
                            ) {
                                TripDetailsSheet(
                                    pickupAddress = rideItem.pickupAddress ?: "Unknown Pickup",
                                    dropAddress = rideItem.dropAddress ?: "Unknown Drop",
                                    fare = rideItem.estimatedPrice ?: "0.00",
                                    onCancelClick = {
                                        viewModel.cancelRide(rideId)
                                    }
                                )
                            }
                        }
                    }
                } else if (rideState is Resource.Error) {
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
fun TripDetailsSheet(
    pickupAddress: String,
    dropAddress: String,
    fare: String,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
    ) {
        Text(
            text = "Trip Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(20.dp))

        // Pickup Row
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Pickup",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pickup",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = pickupAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dropoff Row
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Dropoff",
                tint = Color.Red,
                modifier = Modifier.size(24.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Drop off",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = dropAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(24.dp))

        // Total Fare
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Fare",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "₹$fare",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Payment Method
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Paying via Cash",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Change",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* TODO: Change Payment */ }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cancel Ride Button
        Button(
            onClick = onCancelClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "Cancel",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cancel Ride", fontWeight = FontWeight.Bold)
        }
    }
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
    pickupAddress: String,
    onTripDetailsClick: () -> Unit,
    // ✅ ADDED Callback
    onCallClick: () -> Unit
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

            // Pickup Time & Distance
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
                            tint = Color(0xFFFFD700),
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

                IconButton(
                    onClick = onCallClick, // ✅ Trigger Call
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(20.dp))

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                    onClick = { /* TODO */ },
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(20.dp))

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTripDetailsClick() }
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