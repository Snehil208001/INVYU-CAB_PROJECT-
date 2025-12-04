package com.example.invyucab_project.mainui.travelscreen.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.invyucab_project.core.utils.navigationsbar.AppBottomNavigation
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import com.example.invyucab_project.ui.theme.LightSlateGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(navController: NavController) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Banner
            item {
                TravelStatsBanner()
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

            // Dummy List of Rides
            items(5) { index ->
                val status = if (index == 0) "Ongoing" else if (index % 3 == 0) "Cancelled" else "Completed"
                val price = if (status == "Cancelled") "₹0.00" else "₹240.50"
                TravelHistoryItem(
                    pickup = "Connaught Place, New Delhi",
                    drop = "Sector 62, Noida",
                    date = "Oct ${12 + index}, 2023 • 10:30 AM",
                    price = price,
                    status = status
                )
            }
        }
    }
}

@Composable
fun TravelStatsBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = CabVeryLightMint),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CabMintGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(value = "12", label = "Rides")
            Divider(
                color = CabMintGreen.copy(alpha = 0.3f),
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )
            StatItem(value = "145 km", label = "Distance")
            Divider(
                color = CabMintGreen.copy(alpha = 0.3f),
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )
            StatItem(value = "₹3.2k", label = "Spent")
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

@Composable
fun TravelHistoryItem(
    pickup: String,
    drop: String,
    date: String,
    price: String,
    status: String
) {
    Card(
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
                    text = "Total Paid",
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