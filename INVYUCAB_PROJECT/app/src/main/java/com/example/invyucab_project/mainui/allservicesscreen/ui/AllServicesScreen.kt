package com.example.invyucab_project.mainui.allservicesscreen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// ✅✅✅ START OF FIX ✅✅✅
// This import will now work because the composable exists
import com.example.invyucab_project.core.utils.navigationsbar.AppBottomNavigation
import com.example.invyucab_project.ui.theme.CabMintGreen // Assuming this is your theme color
// ✅✅✅ END OF FIX ✅✅✅

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllServicesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "All Services",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen, // Use your theme color
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // ✅ This call is now valid
            AppBottomNavigation(navController = navController, selectedItem = "Services")
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Text("All Services Screen")
        }
    }
}