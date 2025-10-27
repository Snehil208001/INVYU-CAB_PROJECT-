package com.example.invyucab_project.mainui.allservicesscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Changed from LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Keep grid items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import base filled icons
import androidx.compose.material.icons.outlined.Percent // Import outlined percent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.invyucab_project.mainui.homescreen.ui.AppBottomNavigation
// Removed LightSlateGray import as it's less prominent now
// import com.example.invyucab_project.ui.theme.LightSlateGray
import com.example.invyucab_project.ui.theme.CabMintGreen // Keep for potential future use

// Data class for service items
data class ServiceItem(
    val icon: ImageVector,
    val name: String,
    val iconTint: Color = Color.Black, // Default tint is black now
    // ✅ Use the light green color from the screenshot
    val iconBackgroundColor: Color = Color(0xFFE0F2E9),
    val onClick: () -> Unit = {} // Placeholder for action
)

// Data class to hold a section title and its items
data class ServiceSection(
    val title: String,
    val items: List<ServiceItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllServicesScreen(navController: NavController) {

    // --- Define Service Sections ---
    val rideServices = listOf(
        ServiceItem(Icons.Default.ElectricRickshaw, "Auto"),
        ServiceItem(Icons.Default.LocalTaxi, "Cab Economy"),
        ServiceItem(Icons.Default.TwoWheeler, "Bike"),
        ServiceItem(Icons.Outlined.Percent, "Bike Lite"),
        ServiceItem(Icons.Default.Stars, "Cab Premium")
    )

    val deliveryServices = listOf(
        ServiceItem(Icons.Default.Inventory2, "Parcel"),
        ServiceItem(Icons.Default.Fastfood, "Food Delivery"),
        ServiceItem(Icons.Default.ShoppingCart, "Groceries")
    )

    val travelServices = listOf(
        ServiceItem(Icons.Default.Timer, "Rentals"),
        ServiceItem(Icons.Default.Luggage, "Outstation")
    )

    // Combine sections into a list
    val serviceSections = listOf(
        ServiceSection("Rides", rideServices),
        ServiceSection("Deliveries", deliveryServices),
        ServiceSection("Travel", travelServices)
        // Add more sections as needed
    )
    // -----------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Services", fontWeight = FontWeight.Bold) }, // Updated title
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { AppBottomNavigation(navController = navController) },
        // ✅ Changed background to the very light green matching item background
        containerColor = Color(0xFFF5F5F5) // Or Color.White if preferred
    ) { padding ->
        // Use LazyColumn for overall scrolling of sections
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp) // Padding for the whole list
        ) {
            serviceSections.forEach { section ->
                // Section Header
                item {
                    Text(
                        text = section.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp, top = 8.dp) // Adjust spacing around header
                    )
                }
                // Grid for items in the section
                item {
                    LazyVerticalGrid(
                        // ✅ Use 4 columns
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            // ✅ Height calculation: Adjust row height and spacing as needed
                            .height(((section.items.size + 3) / 4 * 110).dp) // Approx 110dp per item height
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp), // Space between rows
                        horizontalArrangement = Arrangement.spacedBy(10.dp), // Space between columns
                        // Disable scrolling within the grid as LazyColumn handles it
                        userScrollEnabled = false
                    ) {
                        items(section.items) { service ->
                            ServiceItemCard(service = service)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp)) // Space after each grid
                }
            }
        }
    }
}

// Composable for each service item card
@Composable
fun ServiceItemCard(service: ServiceItem) {
    Column(
        modifier = Modifier
            // Use aspect ratio or fixed height if needed, otherwise let content decide
            // .aspectRatio(1f) // Makes it square
            .clip(RoundedCornerShape(16.dp)) // More rounded corners
            .background(service.iconBackgroundColor) // Apply background to the Column
            .clickable(onClick = service.onClick)
            // ✅ Adjusted padding inside the card
            .padding(vertical = 16.dp, horizontal = 4.dp), // More vertical padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        Icon(
            imageVector = service.icon,
            contentDescription = service.name,
            // ✅ Reduced icon size slightly
            modifier = Modifier.size(32.dp),
            tint = service.iconTint // Tint from ServiceItem default (Black)
        )
        Spacer(modifier = Modifier.height(8.dp)) // Reduced space
        Text(
            text = service.name,
            // ✅ Reduced text size slightly
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 1, // Ensure text stays on one line
            overflow = TextOverflow.Ellipsis
        )
    }
}