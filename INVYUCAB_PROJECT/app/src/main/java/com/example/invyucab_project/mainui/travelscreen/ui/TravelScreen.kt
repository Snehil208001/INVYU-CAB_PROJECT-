package com.example.invyucab_project.mainui.travelscreen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
// ✅✅✅ START OF FIX ✅✅✅
// Added the correct import
import com.example.invyucab_project.core.utils.navigationsbar.AppBottomNavigation
// ✅✅✅ END OF FIX ✅✅✅

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Travels") })
        },
        bottomBar = {
            // ✅ This call is now valid
            AppBottomNavigation(navController = navController, selectedItem = "Travel")
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Text("Travel Screen")
        }
    }
}