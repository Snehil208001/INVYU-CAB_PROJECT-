package com.example.invyucab_project.mainui.travelscreen.ui


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.invyucab_project.mainui.homescreen.ui.AppBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Travel Screen")
        }
    }
}