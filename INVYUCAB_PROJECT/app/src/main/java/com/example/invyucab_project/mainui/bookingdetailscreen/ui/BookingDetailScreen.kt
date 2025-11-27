package com.example.invyucab_project.mainui.bookingdetailscreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.invyucab_project.R

@Composable
fun BookingDetailScreen(
    navController: NavController,
    // Defaults updated to match your JSON data
    driverName: String = "Soumadeep Barik",
    vehicleModel: String = "Swift",
    otp: String = "1234",
    rideId: Int = 1,
    riderId: Int = 1,
    driverId: Int = 1,
    role: String = "rider",
    pickupLat: Double = 22.572600,
    pickupLng: Double = 88.363900,
    dropLat: Double = 22.604500,
    dropLng: Double = 88.409000,
    // Additional parameters with defaults for UI
    vehicleNumber: String = "D62626AS",
    rating: String = "4.80",
    price: String = "55.50",
    driverPhotoUrl: String? = "https://example.com/profile.jpg",
    // Previous addresses from HomeScreen/Logs
    pickupLocation: String = "A41, Block D, Paryavaran Complex, Sainik Farm, New Delhi, Delhi 110030, India",
    dropLocation: String = "Dwarka Mor, Vipin Garden, Nawada, Delhi, 110059, India"
) {
    // Ensure we use valid data if empty strings are passed by navigation
    val finalDriverName = if (driverName.isNotBlank() && driverName != "Driver") driverName else "Soumadeep Barik"
    val finalVehicleModel = if (vehicleModel.isNotBlank() && vehicleModel != "Vehicle") vehicleModel else "Swift"
    val finalOtp = if (otp.isNotBlank() && otp != "0000") otp else "1234"

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // --- Header ---
        Text(
            text = "Your Ride",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Driver & Vehicle Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Row: Car Image & ETA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for Car Image
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Car",
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "3 mins", // Estimated time
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = finalVehicleModel,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                // Middle: Vehicle Number
                Text(
                    text = vehicleNumber,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                // Bottom Row: Driver Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver Image
                    if (driverPhotoUrl != null && driverPhotoUrl.startsWith("http")) {
                        AsyncImage(
                            model = driverPhotoUrl,
                            contentDescription = "Driver",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Driver Placeholder",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = finalDriverName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107), // Gold color
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = rating,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Call Button
                    IconButton(
                        onClick = { /* TODO: Call Driver Action */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF4CAF50), CircleShape) // Green background
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Driver",
                            tint = Color.White
                        )
                    }
                }

                // OTP Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE)) // Light Gray
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OTP $finalOtp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Safety Section ---
        Text(
            text = "Ride safe with Invyu",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        TextButton(onClick = { /* TODO: Share ride details */ }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text(
                text = "Share ride details",
                color = Color(0xFF1976D2), // Blue
                fontSize = 14.sp
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)

        // --- Ride Details (Timeline) ---
        Text(
            text = "Ride details",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            // Timeline graphics (Dots and Line)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 12.dp, top = 4.dp)
            ) {
                // Pickup Dot (Green)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                // Connecting Line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp) // Adjust height based on content
                        .background(Color.LightGray)
                )
                // Drop Dot (Red)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFF44336), CircleShape)
                )
            }

            // Timeline Text
            Column {
                // Pickup
                Text(
                    text = "10:00 AM", // Placeholder time
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pickupLocation,
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Drop
                Text(
                    text = "10:30 AM", // Placeholder time
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dropLocation,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Payment Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payment",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            TextButton(onClick = { /* TODO: Change Payment */ }) {
                Text(text = "Change", color = Color(0xFF1976D2))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Cash Icon placeholder
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("₹", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cash ₹$price",
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // --- Cancel Button ---
        Button(
            onClick = {
                // TODO: Handle Cancel Ride
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Red color
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Cancel Ride",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}