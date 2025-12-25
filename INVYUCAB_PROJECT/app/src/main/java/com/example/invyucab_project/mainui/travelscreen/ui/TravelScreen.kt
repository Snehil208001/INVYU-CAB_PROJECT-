package com.example.invyucab_project.mainui.travelscreen.ui

import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.core.utils.navigationsbar.AppBottomNavigation
import com.example.invyucab_project.mainui.ridehistoryscreen.viewmodel.RideHistoryViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import com.example.invyucab_project.ui.theme.LightSlateGray
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(
    navController: NavController,
    viewModel: RideHistoryViewModel = hiltViewModel()
) {
    val rideHistory by viewModel.rideHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 1. Calculate Stats: Total Rides
    val totalRides = rideHistory.size

    // 2. Calculate Stats: Total Spent
    val totalSpent = remember(rideHistory) {
        rideHistory.sumOf {
            it.actualPrice?.toDoubleOrNull() ?: it.estimatedPrice?.toDoubleOrNull() ?: 0.0
        }
    }

    // 3. Calculate Stats: Total Distance
    val totalDistance = remember(rideHistory) {
        var totalMeters = 0.0f
        val results = FloatArray(1)

        for (ride in rideHistory) {
            // ✅ FIX: Use Double values directly from UI Model
            val startLat = ride.pickupLat
            val startLng = ride.pickupLng
            val endLat = ride.dropLat
            val endLng = ride.dropLng

            if (startLat != 0.0 && startLng != 0.0 && endLat != 0.0 && endLng != 0.0) {
                Location.distanceBetween(startLat, startLng, endLat, endLng, results)
                totalMeters += results[0]
            }
        }
        val distKm = totalMeters / 1000
        String.format("%.1f km", distKm)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Travels",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            AppBottomNavigation(navController = navController, selectedItem = "Travel")
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CabMintGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Banner
                item {
                    TravelStatsBanner(
                        rides = totalRides.toString(),
                        distance = totalDistance,
                        spent = String.format("₹%.0f", totalSpent)
                    )
                }

                // Section Header
                item {
                    Text(
                        text = "Recent Activity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (rideHistory.isEmpty()) {
                    item {
                        Text(
                            text = "No travel history found.",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(rideHistory) { ride ->

                        // ✅ FIX: Use the resolved address from ViewModel
                        val pickup = ride.pickupAddress
                        val drop = ride.dropAddress

                        val dateStr = formatParamsDate(ride.requestedAt ?: ride.startedAt)
                        val price = "₹${ride.actualPrice ?: ride.estimatedPrice ?: "0.00"}"

                        val rawStatus = ride.status?.lowercase() ?: ""
                        val displayStatus = rawStatus.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                        }.ifEmpty { "Unknown" }

                        // ✅ Check if the ride is trackable
                        val isTrackable = rawStatus == "in_progress" || rawStatus == "accepted"

                        TravelHistoryItem(
                            pickup = pickup,
                            drop = drop,
                            date = dateStr,
                            price = price,
                            status = displayStatus,
                            isTrackable = isTrackable, // Pass trackable state
                            onClick = {
                                if (isTrackable) {
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
                                        // ✅ FIX: Use Double values directly from UI Model
                                        pickupLat = ride.pickupLat,
                                        pickupLng = ride.pickupLng,
                                        dropLat = ride.dropLat,
                                        dropLng = ride.dropLng
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

@Composable
fun TravelStatsBanner(rides: String, distance: String, spent: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = CabVeryLightMint),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CabMintGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(value = rides, label = "Rides")
            Divider(
                color = CabMintGreen.copy(alpha = 0.3f),
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )
            StatItem(value = distance, label = "Est. Distance")
            Divider(
                color = CabMintGreen.copy(alpha = 0.3f),
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )
            StatItem(value = spent, label = "Spent")
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = CabMintGreen
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelHistoryItem(
    pickup: String,
    drop: String,
    date: String,
    price: String,
    status: String,
    isTrackable: Boolean, // ✅ New Parameter
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date and Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                StatusBadge(status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = CabMintGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pickup,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .padding(start = 7.dp, top = 2.dp, bottom = 2.dp)
                    .height(16.dp)
                    .width(2.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            // Drop
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = drop,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = LightSlateGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTrackable) "Estimated Fare" else "Total Paid", // ✅ Conditional Label
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // ✅ "Track Ride" Button for actionable rides
            if (isTrackable) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Track",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Track Ride",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, bg, icon) = when (status) {
        "Completed" -> Triple(CabMintGreen, CabVeryLightMint, Icons.Default.CheckCircle)
        "Cancelled" -> Triple(Color.Red, Color(0xFFFFEBEE), Icons.Default.Warning)
        else -> Triple(Color(0xFFFFA500), Color(0xFFFFF3E0), Icons.Default.Navigation)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatParamsDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}