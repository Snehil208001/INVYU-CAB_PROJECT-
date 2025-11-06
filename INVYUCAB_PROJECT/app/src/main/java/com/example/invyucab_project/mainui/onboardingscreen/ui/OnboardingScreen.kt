package com.example.invyucab_project.mainui.onboardingscreen.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // ✅ ADDED Import
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.navigations.Screen
// ✅ ADDED Import
import com.example.invyucab_project.mainui.onboardingscreen.viewmodel.OnboardingViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

data class OnboardingItem(
    val imageRes: Int,
    val title: String,
    val description: String
)

// A list containing the data for all onboarding screens
val onboardingPages = listOf(
    OnboardingItem(
        // ✅ CORRECTED: Typo in drawable name
        imageRes = R.drawable.onboqrding1,
        title = "Request Ride",
        // ✅ UPDATED: New, more engaging description
        description = "Get picked up by a friendly driver from our trusted community, right at your doorstep."
    ),
    OnboardingItem(
        // ✅ CORRECTED: Typo in drawable name
        imageRes = R.drawable.onboarding2,
        title = "Confirm Your Driver",
        // ✅ UPDATED: New, more engaging description
        description = "Our vast network ensures you a comfortable, safe, and affordable ride every time."
    ),
    OnboardingItem(
        // ✅ CORRECTED: Typo in drawable name
        imageRes = R.drawable.onboarding3,
        title = "Track Your Ride",
        // ✅ UPDATED: New, more engaging description
        description = "Watch your driver's approach in real-time and know exactly when they'll arrive."
    )
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel() // ✅ INJECTED ViewModel
) {
    val pagerState = rememberPagerState { onboardingPages.size }

    Scaffold(
        containerColor = CabVeryLightMint,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    OnboardingPage(item = onboardingPages[page])
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PagerIndicator(pagerState = pagerState)
                    Spacer(modifier = Modifier.height(30.dp))
                    AnimatedVisibility(visible = pagerState.currentPage == onboardingPages.size - 1) {
                        Button(
                            onClick = {
                                // ✅✅✅ START OF FIX ✅✅✅
                                viewModel.onGetStartedClicked() // Save the flag
                                // ✅✅✅ END OF FIX ✅✅✅
                                navController.navigate(Screen.AuthScreen.route) {
                                    popUpTo(Screen.OnboardingScreen.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp)
                                .height(50.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
                        ) {
                            Text(
                                "GET STARTED!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun OnboardingPage(item: OnboardingItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ✅ The wrapping Box has been removed.
        // A size modifier is now applied directly to the Image.
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            modifier = Modifier.size(300.dp), // Constrains the image size
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = item.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = item.description,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        // Spacer to push content up from the button area
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun PagerIndicator(pagerState: PagerState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) CabMintGreen else Color.LightGray
            val width = if (pagerState.currentPage == iteration) 25.dp else 10.dp
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(10.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}