package com.example.invyucab_project.mainui.rideselectionscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.mainui.rideselectionscreen.viewmodel.RideOption
import com.example.invyucab_project.mainui.rideselectionscreen.viewmodel.RideSelectionViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.LightSlateGray
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideSelectionScreen(
    navController: NavController,
    viewModel: RideSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.pickupLocation, 12f)
    }

    // This will animate the camera to show the whole route
    LaunchedEffect(uiState.routePolyline) {
        if (uiState.routePolyline.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            uiState.routePolyline.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
            )
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // Draw the route
                if (uiState.routePolyline.isNotEmpty()) {
                    Polyline(
                        points = uiState.routePolyline,
                        color = Color.Black,
                        width = 10f
                    )
                }
                // Add markers
                Marker(
                    state = MarkerState(position = uiState.pickupLocation),
                    title = "Pickup"
                )
                uiState.dropLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Drop"
                    )
                }
            }

            // Top Bar with location info
            LocationTopBar(
                pickup = uiState.pickupDescription,
                drop = uiState.dropDescription,
                onBack = { navController.navigateUp() },
                onAddStop = { /* TODO */ }
            )

            // Bottom Sheet
            RideOptionsBottomSheet(
                rideOptions = viewModel.rideOptions
            )
        }
    }
}

@Composable
fun LocationTopBar(
    pickup: String,
    drop: String,
    onBack: () -> Unit,
    onAddStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            // Dotted line graphic
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CabMintGreen))
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.Gray))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Black))
            }
            // Location text
            Column(modifier = Modifier.weight(1f)) {
                Text(pickup, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(drop, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            }
            // Add stop button
            IconButton(onClick = onAddStop) {
                Icon(Icons.Default.Add, contentDescription = "Add stop", modifier = Modifier.clip(CircleShape).background(LightSlateGray).padding(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.RideOptionsBottomSheet(rideOptions: List<RideOption>) {
    var selectedRideId by remember { mutableStateOf(1) } // Default to Bike

    BottomSheetScaffold(
        sheetContent = {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp) // Limit height
                ) {
                    items(rideOptions) { ride ->
                        RideOptionItem(
                            ride = ride,
                            isSelected = ride.id == selectedRideId,
                            onClick = { selectedRideId = ride.id }
                        )
                    }
                }
                // Banner
                Text("UNLIMITED Discounts! Buy Pass Now >", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                // Payment & Book Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = "Payment")
                        Text("Cash", fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                    Text("% Offers", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { /* TODO: Book Ride */ },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)) // Yellow
                ) {
                    Text("Book ${rideOptions.find { it.id == selectedRideId }?.name ?: ""}", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        sheetContainerColor = Color.White,
        sheetPeekHeight = 350.dp, // Adjust as needed
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {}
}

@Composable
fun RideOptionItem(ride: RideOption, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) CabMintGreen.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(ride.icon, contentDescription = ride.name, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(ride.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(ride.description, fontSize = 13.sp, color = Color.Gray)
            }
        }
        Text("₹${ride.price}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}