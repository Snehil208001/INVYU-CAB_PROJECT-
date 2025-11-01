package com.example.invyucab_project.mainui.otpscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen // ✅ Import Screen
import com.example.invyucab_project.mainui.otpscreen.viewmodel.OtpViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    navController: NavController,
    viewModel: OtpViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Verification", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = CabVeryLightMint
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "Enter your OTP code here",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                "Sent to ${viewModel.fullPhoneNumber}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            OtpTextField(
                otpText = viewModel.otp,
                onOtpTextChange = { value -> viewModel.onOtpChange(value) }
            )

            // ✅ ADDED: Show error text
            if (viewModel.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = viewModel.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    // ✅ MODIFIED: Pass both navigation paths to the ViewModel
                    viewModel.onVerifyClicked(
                        // Path 1: (Sign In) Navigate to Home
                        onNavigateToHome = {
                            navController.navigate(Screen.HomeScreen.route) {
                                // ✅ *** CORRECTED LINE ***
                                popUpTo(Screen.AuthScreen.route) { inclusive = true }
                            }
                        },
                        // ✅ *** MODIFIED THIS LAMBDA ***
                        // Path 2: (Sign Up) Navigate to Home after successful verification
                        onNavigateToDetails = {
                            navController.navigate(Screen.HomeScreen.route) {
                                // Pop AuthScreen, UserDetailsScreen, and this OtpScreen
                                popUpTo(Screen.AuthScreen.route) { inclusive = true }
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                enabled = viewModel.otp.length == 4
            ) {
                Text("Verify Now", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// ... (OtpTextField and OtpChar composables are unchanged) ...
@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 4,
    onOtpTextChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    // This is key for interactivity: it automatically opens the keyboard.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = TextFieldValue(otpText, selection = TextRange(otpText.length)),
        onValueChange = {
            // This is the core loop: send changes up to the ViewModel.
            if (it.text.length <= otpCount) {
                onOtpTextChange.invoke(it.text)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(otpCount) { index ->
                    val char = otpText.getOrNull(index)?.toString() ?: ""
                    // This provides visual feedback for the current input position.
                    val isFocused = otpText.length == index
                    OtpChar(
                        char = char,
                        isFocused = isFocused
                    )
                    if (index < otpCount - 1) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }
    )
}

@Composable
private fun OtpChar(
    char: String,
    isFocused: Boolean
) {
    val borderColor = if (isFocused) CabMintGreen else Color.LightGray
    Box(
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color.White, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontSize = 22.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}