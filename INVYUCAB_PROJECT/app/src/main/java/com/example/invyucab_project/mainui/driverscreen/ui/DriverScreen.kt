package com.example.invyucab_project.mainui.driverscreen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // ✅ IMPORT ADDED
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel // ✅ IMPORT ADDED
import com.example.invyucab_project.mainui.driverscreen.viewmodel.DriverViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    navController: NavController,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val isActive = viewModel.isActive

    // ✅✅✅ START OF FIX ✅✅✅
    // We must collect navigation events here, just like in the other screens.
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    // The popUpTo logic now lives in the UI, not the ViewModel
                    navController.navigate(event.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    // Handle snackbars if you add them later
                }
            }
        }
    }
    // ✅✅✅ END OF FIX ✅✅✅

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isActive) "You are Active" else "You are Inactive",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Switch(
                        checked = isActive,
                        onCheckedChange = { viewModel.onActiveToggleChanged(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CabMintGreen,
                            checkedTrackColor = Color.White,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen
                )
            )
        },
        containerColor = CabVeryLightMint
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Driver",
                    modifier = Modifier.size(100.dp),
                    tint = CabMintGreen
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isActive) {
                        "Go Inactive to Stop Receiving Rides"
                    } else {
                        "Go Active to Start Receiving Rides"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.onLogoutClicked() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Logout")
                }
            }
        }
    }
}