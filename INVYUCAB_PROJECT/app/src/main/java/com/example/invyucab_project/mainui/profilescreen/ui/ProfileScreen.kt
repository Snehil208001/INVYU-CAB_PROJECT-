package com.example.invyucab_project.mainui.profilescreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle // Keep general filled icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // Import hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen // Import Screen
import com.example.invyucab_project.domain.model.ProfileOption
import com.example.invyucab_project.mainui.homescreen.ui.AppBottomNavigation
import com.example.invyucab_project.mainui.profilescreen.viewmodel.ProfileViewModel // Import ProfileViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.LightSlateGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel() // Inject ViewModel
) {
    // Get user profile data and options from ViewModel
    val userProfile by viewModel.userProfile.collectAsState()
    val profileOptions = viewModel.profileOptions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { AppBottomNavigation(navController = navController) },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightSlateGray.copy(alpha = 0.3f)),
        ) {
            // Header Section - Pass data from ViewModel state
            item {
                ProfileHeader(
                    name = userProfile.name,
                    phone = userProfile.phone
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Options Section - Use options from ViewModel
            items(profileOptions) { option ->
                ProfileOptionItem(
                    option = option,
                    navController = navController,
                    viewModel = viewModel // ✅ PASS ViewModel
                )
            }
        }
    }
}

// Composable for the Profile Header
@Composable
fun ProfileHeader(name: String, phone: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Placeholder for Profile Picture
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LightSlateGray),
            tint = Color.Gray
            // TODO: Replace with Coil Image loader if profilePicUrl is available
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = phone,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

// Composable for each item in the profile options list
@Composable
fun ProfileOptionItem(
    option: ProfileOption,
    navController: NavController,
    viewModel: ProfileViewModel // ✅ ACCEPT ViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = {
                // ✅✅✅ START OF MODIFICATION ✅✅✅
                // Handle navigation directly
                when (option.title) {
                    "Edit Profile" -> navController.navigate(Screen.EditProfileScreen.route)
                    "Payment Methods" -> navController.navigate(Screen.PaymentMethodScreen.route)
                    // "Ride History" -> navController.navigate(Screen.RideHistoryScreen.route) // Example
                    "Logout" -> {
                        viewModel.logout() // Call the logout function
                        // ✅ MODIFIED: Navigate to OnboardingScreen instead of AuthScreen
                        navController.navigate(Screen.OnboardingScreen.route) {
                            // Clear the entire back stack
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    else -> option.onClick() // Fallback for other clicks
                }
                // ✅✅✅ END OF MODIFICATION ✅✅✅
            })
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.title,
            tint = CabMintGreen
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = option.title,
            fontSize = 16.sp,
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Go to ${option.title}",
            tint = Color.Gray.copy(alpha = 0.7f)
        )
    }
    Divider(color = LightSlateGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp)) // Added thin divider
}