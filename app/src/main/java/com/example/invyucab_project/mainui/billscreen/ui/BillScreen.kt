package com.example.invyucab_project.mainui.billscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.ui.theme.CabLightGreen
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabPrimaryGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@Composable
fun BillScreen(
    navController: NavController,
    fare: String,
    role: String,
    pickupAddress: String,
    dropAddress: String
) {
    // Determine target screen based on user role
    val targetScreen = if (role == "driver") Screen.DriverScreen.route else Screen.HomeScreen.route

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CabVeryLightMint) // Using app theme background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Success Icon Area
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(CabLightGreen),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(CabMintGreen), // Inner circle
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ride Completed!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = CabPrimaryGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (role == "driver") "You earned" else "Total Payment",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Text(
            text = "₹$fare",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp
            ),
            color = Color.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Trip Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Trip Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Timeline Graphics
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Pickup",
                            tint = CabPrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        // Vertical Line
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(34.dp)
                                .background(Color.LightGray)
                        )
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Drop",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Address Texts
                    Column(modifier = Modifier.weight(1f)) {
                        // Pickup
                        Text(
                            text = "Pickup",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = pickupAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Drop
                        Text(
                            text = "Drop-off",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = dropAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Home Button
        Button(
            onClick = {
                navController.navigate(targetScreen) {
                    popUpTo(0) // Clear entire back stack
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CabPrimaryGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back to Home",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun BillScreenPreview() {
    BillScreen(
        navController = rememberNavController(),
        fare = "250.00",
        role = "rider",
        pickupAddress = "Patna Junction, Bihar",
        dropAddress = "Gandhi Maidan, Patna"
    )
}