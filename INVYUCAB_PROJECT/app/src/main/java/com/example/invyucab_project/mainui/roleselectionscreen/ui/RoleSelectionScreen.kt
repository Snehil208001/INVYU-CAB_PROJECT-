package com.example.invyucab_project.mainui.roleselectionscreen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.roleselectionscreen.viewmodel.RoleSelectionViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@Composable
fun RoleSelectionScreen(
    navController: NavController,
    viewModel: RoleSelectionViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = viewModel.isLoading.value
    val apiError = viewModel.apiError.value

    // --- Event Collection ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    // Clear the stack and navigate to the new screen
                    navController.navigate(event.route) {
                        popUpTo(Screen.AuthScreen.route) { inclusive = true }
                    }
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // --- Error Handling ---
    LaunchedEffect(apiError) {
        if (apiError != null) {
            snackbarHostState.showSnackbar(
                message = apiError,
                duration = SnackbarDuration.Short
            )
            viewModel.clearApiError()
        }
    }

    Scaffold(
        containerColor = CabVeryLightMint,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome, ${viewModel.name ?: "User"}!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "How would you like to use our service?",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // RoleCard for Rider
                RoleCard(
                    title = "Rider",
                    description = "Find and book rides.",
                    iconRes = R.drawable.onboqrding1, // Replace with actual rider icon
                    onClick = { viewModel.onRoleSelected("Rider") },
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // RoleCard for Driver
                RoleCard(
                    title = "Driver",
                    description = "Offer rides and earn money.",
                    iconRes = R.drawable.onboarding2, // Replace with actual driver icon
                    onClick = { viewModel.onRoleSelected("Driver") },
                    isLoading = isLoading
                )
            }

            // Loading overlay
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CabMintGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleCard(
    title: String,
    description: String,
    iconRes: Int,
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CabMintGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
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