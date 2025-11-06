package com.example.invyucab_project.mainui.otpscreen.ui

import android.app.Activity
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun OtpScreen(
    navController: NavController,
    viewModel: OtpViewModel = hiltViewModel()
) {
    // ✅ Get the activity from context
    val activity = LocalContext.current as Activity
    val keyboardController = LocalSoftwareKeyboardController.current

    // ✅ Send the OTP as soon as the screen is composed
    LaunchedEffect(Unit) {
        viewModel.sendOtp(activity)
    }

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
        Box(modifier = Modifier.fillMaxSize()) { // ✅ Box for loading overlay
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
                    "Sent to +91 ${viewModel.fullPhoneNumber}", // ✅ Added +91
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))
                OtpTextField(
                    otpText = viewModel.otp,
                    onOtpTextChange = { value -> viewModel.onOtpChange(value) },
                    keyboardController = keyboardController
                )

                if (viewModel.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.error!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        // ✅ MODIFIED: Call the renamed 'onVerifyClicked' function
                        viewModel.onVerifyClicked(
                            // Path 1: (Sign Up) Navigate to Role Selection
                            onNavigateToRoleSelection = { phone, email, name, gender, dob ->
                                navController.navigate(
                                    Screen.RoleSelectionScreen.createRoute(
                                        phone = phone,
                                        email = email,
                                        name = name,
                                        gender = gender,
                                        dob = dob
                                    )
                                ) {
                                    // ✅✅✅ THIS IS THE FIX ✅✅✅
                                    // This removes OtpScreen from the back stack,
                                    // so pressing back on RoleSelectionScreen
                                    // will go to UserDetailsScreen.
                                    popUpTo(Screen.OtpScreen.route) {
                                        inclusive = true
                                    }
                                }
                            },
                            // Path 2: (Sign In) Navigate to Home
                            onNavigateToHome = {
                                navController.navigate(Screen.HomeScreen.route) {
                                    // This is correct for Sign-In, it
                                    // clears the whole auth flow.
                                    popUpTo(Screen.AuthScreen.route) { inclusive = true }
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                    enabled = viewModel.otp.length == 6 && !viewModel.isLoading // ✅ Check for 6 digits
                ) {
                    // ✅ MODIFIED: Show loader or text
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("Verify Now", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 6, // ✅ Changed to 6
    onOtpTextChange: (String) -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = TextFieldValue(otpText, selection = TextRange(otpText.length)),
        onValueChange = {
            if (it.text.length <= otpCount) {
                onOtpTextChange.invoke(it.text)
                // ✅ ADDED: Hide keyboard when 6 digits are entered
                if (it.text.length == otpCount) {
                    keyboardController?.hide()
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(otpCount) { index ->
                    val char = otpText.getOrNull(index)?.toString() ?: ""
                    val isFocused = otpText.length == index
                    OtpChar(
                        char = char,
                        isFocused = isFocused
                    )
                    if (index < otpCount - 1) {
                        Spacer(modifier = Modifier.width(12.dp)) // ✅ Reduced space
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
            .size(48.dp) // ✅ Reduced size
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