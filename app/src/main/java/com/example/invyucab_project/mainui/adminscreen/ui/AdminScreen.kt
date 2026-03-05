package com.example.invyucab_project.mainui.adminscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.mainui.adminscreen.viewmodel.AdminViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Events ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        if (event.route.contains("auth")) popUpTo(0)
                    }
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Professional Off-White
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // ✅ UPDATED HEADER with System Padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CabMintGreen)
                    // This adds padding for the status bar (Time/Battery) so content doesn't overlap
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Administrator",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Notification & Logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Alerts", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.onLogoutClicked() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // item 1: Floating Stats Layer
            item {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    // Green Extension Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(CabMintGreen, CabMintGreen)
                                ),
                                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                            )
                    )

                    // Floating Cards Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(
                            label = "Drivers",
                            value = "124",
                            icon = Icons.Default.DirectionsCar,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard(
                            label = "Users",
                            value = "8.2k",
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard(
                            label = "Rev.",
                            value = "₹12k",
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // item 2: Quick Actions Title
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            // item 3: Action Grid
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActionCardBig(
                            title = "Manage Fleet",
                            subtitle = "Drivers & Cars",
                            icon = Icons.Default.LocalTaxi,
                            color = CabMintGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onManageDriversClicked() }
                        )
                        ActionCardBig(
                            title = "User Base",
                            subtitle = "Riders & Profiles",
                            icon = Icons.Default.Group,
                            color = Color(0xFF5EBA7D),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onManageRidersClicked() }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActionCardBig(
                            title = "Ride History",
                            subtitle = "Track Trips",
                            icon = Icons.Default.History,
                            color = Color(0xFF4CA6A6),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onViewRidesClicked() }
                        )
                        ActionCardBig(
                            title = "Settings",
                            subtitle = "App Config",
                            icon = Icons.Default.Settings,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            onClick = { /* Future */ }
                        )
                    }
                }
            }

            // item 4: Recent Activity Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CabMintGreen,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
            }

            // item 5: Mock Activity List
            items(5) { index ->
                ActivityItem(index)
            }
        }
    }
}

// ------------------------------------
// UI COMPONENTS
// ------------------------------------

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = CabMintGreen.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CabMintGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActionCardBig(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Circle Decoration
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 60.dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
                    .align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityItem(index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(CabVeryLightMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (index % 2 == 0) Icons.Default.PersonAdd else Icons.Default.LocalTaxi,
                contentDescription = null,
                tint = CabMintGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (index % 2 == 0) "New Rider Registered" else "Ride #102${index} Completed",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = "2 mins ago",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}