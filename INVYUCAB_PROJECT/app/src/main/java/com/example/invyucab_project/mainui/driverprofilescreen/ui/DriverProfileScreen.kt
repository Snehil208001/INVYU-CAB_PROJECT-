package com.example.invyucab_project.mainui.driverprofilescreen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.driverprofilescreen.viewmodel.DriverProfileViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    navController: NavController,
    viewModel: DriverProfileViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Observe the dynamic name from ViewModel
    val driverName by viewModel.driverName.collectAsState()
    // ✅ Observe the dynamic phone number from ViewModel
    val driverPhoneNumber by viewModel.driverPhoneNumber.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(Screen.AuthScreen.route) { inclusive = true }
                    }
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DriverProfileTopAppBar(
                onBackClicked = { navController.popBackStack() }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(CabMintGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Driver Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Display the Dynamic Name
            Text(
                text = driverName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // ✅ Display the Phone Number
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = driverPhoneNumber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Menu Options ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ProfileOptionRow(text = "Profile Setting", onClick = { /* TODO */ })

                ProfileOptionRow(
                    text = "Documents",
                    onClick = {
                        navController.navigate(Screen.DriverDocumentsScreen.route)
                    }
                )

                ProfileOptionRow(text = "My Earning", onClick = { /* TODO */ })
                ProfileOptionRow(text = "Car", onClick = { /* TODO */ })
                ProfileOptionRow(text = "Push Notification", onClick = { /* TODO */ })
                ProfileOptionRow(text = "Complain", onClick = { /* TODO */ })

                // ✅ UPDATED: Navigation for About Us
                ProfileOptionRow(
                    text = "About Us",
                    onClick = {
                        navController.navigate(Screen.AboutUsScreen.route)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onLogoutClicked() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverProfileTopAppBar(
    onBackClicked: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Profile",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CabMintGreen,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
private fun ProfileOptionRow(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CabMintGreen.copy(alpha = 0.3f)),
        color = CabMintGreen.copy(alpha = 0.1f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go to $text",
                tint = CabMintGreen
            )
        }
    }
}