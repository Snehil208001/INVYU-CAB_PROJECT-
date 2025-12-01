package com.example.invyucab_project.mainui.ridehistoryscreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.data.models.RiderRideHistoryItem
import com.example.invyucab_project.mainui.ridehistoryscreen.viewmodel.RideHistoryViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen(
    navController: NavController,
    viewModel: RideHistoryViewModel = hiltViewModel()
) {
    val rideHistory by viewModel.rideHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // ✅ SORTING:
    // Top (0): Requested, Accepted, In Progress
    // Bottom (1): Completed, Cancelled
    val sortedHistory = remember(rideHistory) {
        rideHistory.sortedBy { ride ->
            val status = ride.status?.lowercase() ?: ""
            if (status == "completed" || status == "cancelled") 1 else 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Rides", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CabMintGreen
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Failed to load history",
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.fetchRideHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
                sortedHistory.isEmpty() -> {
                    Text(
                        text = "You haven't taken any rides yet.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedHistory) { ride ->
                            RideHistoryItemCard(
                                ride = ride,
                                onClick = {
                                    val status = ride.status?.lowercase() ?: ""

                                    // ✅ NAVIGATION FIX: Only for "accepted" or "in_progress"
                                    if (status == "accepted" || status == "in_progress") {

                                        // Safe Arguments to prevent crashes
                                        val safeDriverName = if (!ride.driverName.isNullOrBlank()) ride.driverName else "Unknown Driver"
                                        val safeModel = if (!ride.model.isNullOrBlank()) ride.model else "Unknown Vehicle"
                                        val safeOtp = "Unavailable"

                                        val route = Screen.BookingDetailScreen.createRoute(
                                            driverName = safeDriverName,
                                            vehicleModel = safeModel,
                                            otp = safeOtp,
                                            rideId = ride.rideId,
                                            riderId = ride.riderId ?: 0,
                                            driverId = ride.driverId ?: 0,
                                            role = "rider",
                                            pickupLat = ride.pickupLatitude?.toDoubleOrNull() ?: 0.0,
                                            pickupLng = ride.pickupLongitude?.toDoubleOrNull() ?: 0.0,
                                            dropLat = ride.dropLatitude?.toDoubleOrNull() ?: 0.0,
                                            dropLng = ride.dropLongitude?.toDoubleOrNull() ?: 0.0
                                        )
                                        navController.navigate(route)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryItemCard(
    ride: RiderRideHistoryItem,
    onClick: () -> Unit
) {
    val statusColor = when (ride.status?.lowercase()) {
        "completed" -> CabMintGreen
        "accepted" -> Color.Blue
        "cancelled" -> Color.Red
        "in_progress" -> Color(0xFFFFA500) // Orange
        else -> Color.DarkGray
    }

    val dateStr = ride.requestedAt?.take(10) ?: "N/A"
    val timeStr = if ((ride.requestedAt?.length ?: 0) > 16) {
        ride.requestedAt?.substring(11, 16)
    } else {
        ""
    }

    Card(
        onClick = onClick, // ✅ Using Card's native onClick for reliability
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- Header: Date, Time, and Price ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$dateStr • $timeStr",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "₹${ride.actualPrice ?: ride.estimatedPrice ?: "0.00"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Status Badge ---
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = ride.status?.uppercase() ?: "UNKNOWN",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // --- Driver & Vehicle Info ---
            if (ride.driverId != null && ride.driverId != 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.LightGray.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Driver",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = ride.driverName ?: "Unknown Driver",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ride.driverRating ?: "4.5",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = CabMintGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${ride.model ?: "Car"} • ${ride.vehicleNumber ?: "N/A"}",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Driver not assigned",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}