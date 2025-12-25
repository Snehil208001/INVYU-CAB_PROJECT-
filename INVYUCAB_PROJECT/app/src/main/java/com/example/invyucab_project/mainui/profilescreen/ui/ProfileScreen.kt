package com.example.invyucab_project.mainui.profilescreen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.model.ProfileOption
import com.example.invyucab_project.core.utils.navigationsbar.AppBottomNavigation
import com.example.invyucab_project.mainui.profilescreen.viewmodel.ProfileViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // ✅ Collects the dynamic user state from ViewModel
    val userProfile by viewModel.userProfile.collectAsState()
    val profileOptions = viewModel.profileOptions

    val displayOptions = profileOptions.filter { it.title != "Logout" }

    Scaffold(
        bottomBar = {
            AppBottomNavigation(navController = navController, selectedItem = "Profile")
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Green Header Background with Curved Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        color = CabMintGreen,
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. Top Bar Content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // 3. Avatar (Overlapping)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(100.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .background(CabMintGreen, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. User Name (Dynamic)
                Text(
                    text = userProfile.name, // ✅ Shows the name fetched from API
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Options List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayOptions) { option ->
                        ProfileOptionCard(
                            option = option,
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }

                // 6. Logout Button
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.OnboardingScreen.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionCard(
    option: ProfileOption,
    navController: NavController,
    viewModel: ProfileViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                when (option.title) {
                    "Edit Profile" -> navController.navigate(Screen.EditProfileScreen.route)
                    "Payment Methods" -> navController.navigate(Screen.PaymentMethodScreen.route)
                    "Ride History" -> navController.navigate(Screen.RideHistoryScreen.route)
                    "Logout" -> { /* Handled by bottom button */ }
                    else -> option.onClick()
                }
            }),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CabMintGreen.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, CabMintGreen.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = CabMintGreen
            )
        }
    }
}