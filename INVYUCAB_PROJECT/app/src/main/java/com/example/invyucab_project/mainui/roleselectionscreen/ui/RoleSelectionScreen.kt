package com.example.invyucab_project.mainui.roleselectionscreen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.roleselectionscreen.viewmodel.RoleSelectionViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    navController: NavController,
    viewModel: RoleSelectionViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ ADDED: Show snackbar on API error
    LaunchedEffect(viewModel.apiError) {
        viewModel.apiError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = CabVeryLightMint,
        snackbarHost = { SnackbarHost(snackbarHostState) }, // ✅ ADDED
        topBar = {
            TopAppBar(
                title = { Text("Select Your Role", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        // ✅ ADDED: Box for loading indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(), // ✅ THIS IS THE FIX
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Welcome, ${viewModel.name ?: "User"}!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "How will you be using INVYU?",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))

                // 5.1: Rider Role
                RoleCard(
                    icon = Icons.Default.Person,
                    title = "Rider",
                    description = "Book rides and get to your destination.",
                    enabled = !viewModel.isLoading, // ✅ ADDED
                    onClick = {
                        viewModel.onRoleSelected(role = "Rider") { route ->
                            navController.navigate(route) {
                                // 5.1.2: Registration complete, clear auth stack
                                popUpTo(Screen.AuthScreen.route) { inclusive = true }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5.3: Driver Role (Renamed to Pilot)
                RoleCard(
                    icon = Icons.Default.DirectionsCar,
                    title = "Pilot", // ✅ CHANGED
                    description = "Pilot rides and earn money.", // ✅ CHANGED
                    enabled = !viewModel.isLoading,
                    onClick = {
                        // Note: We still pass "Driver" to the ViewModel
                        // to avoid breaking the navigation logic.
                        viewModel.onRoleSelected(role = "Driver") { route ->
                            // 5.3.1: Navigate to DriverDetailsScreen
                            navController.navigate(route)
                        }
                    }
                )

                // ✅ REMOVED Admin Role Card
                /*
                Spacer(modifier = Modifier.height(20.dp))

                // 5.2: Admin Role
                RoleCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Admin",
                    description = "Manage app operations.",
                    enabled = !viewModel.isLoading, // ✅ ADDED
                    onClick = {
                        viewModel.onRoleSelected(role = "Admin") { route ->
                            navController.navigate(route) {
                                // 5.2.2: Registration complete, clear auth stack
                                popUpTo(Screen.AuthScreen.route) { inclusive = true }
                            }
                        }
                    }
                )
                */
            }

            // ✅ ADDED: Loading Indicator
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = CabMintGreen)
            }
        }
    }
}

@Composable
fun RoleCard(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean, // ✅ ADDED
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick), // ✅ MODIFIED
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CabMintGreen,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}