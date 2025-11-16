package com.example.invyucab_project.mainui.authscreen.ui

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.domain.model.AuthTab
import com.example.invyucab_project.domain.model.GoogleSignInState
import com.example.invyucab_project.mainui.authscreen.viewmodel.AuthViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val googleSignInState by viewModel.googleSignInState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- Event Collection ---
    // This LaunchedEffect listens for one-time events from the ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                // ✅ --- THIS IS THE FIX ---
                // Add an else branch to handle all other events (like NavigateBack)
                else -> {}
                // ✅ --- END OF FIX ---
            }
        }
    }

    // ... (Your Google Sign-In LaunchedEffect, if you re-enable it) ...

    Scaffold(
        containerColor = CabVeryLightMint,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AuthHeader()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-50).dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        AuthTabs(
                            selectedTab = viewModel.selectedTab,
                            onTabSelected = { viewModel.onTabSelected(it) },
                            isLoading = viewModel.isLoading.value // Read .value
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Crossfade(targetState = viewModel.selectedTab, label = "AuthFormCrossfade") { tab ->
                            when (tab) {
                                AuthTab.SIGN_UP -> SignUpForm(viewModel, googleSignInState, keyboardController)
                                AuthTab.SIGN_IN -> SignInForm(viewModel, keyboardController)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... (AuthHeader, AuthTabs, AuthTabItem composables are unchanged) ...
@Composable
fun AuthHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cityscape_background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AuthTabs(
    selectedTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AuthTabItem(
            text = "Register",
            isSelected = selectedTab == AuthTab.SIGN_UP,
            onClick = { if (!isLoading) onTabSelected(AuthTab.SIGN_UP) }
        )
        AuthTabItem(
            text = "Sign In",
            isSelected = selectedTab == AuthTab.SIGN_IN,
            onClick = { if (!isLoading) onTabSelected(AuthTab.SIGN_IN) }
        )
    }
}

@Composable
fun AuthTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.Gray,
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CabMintGreen)
            )
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpForm(
    viewModel: AuthViewModel,
    googleSignInState: GoogleSignInState,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    Column {
        OutlinedTextField(
            value = viewModel.signUpPhone,
            onValueChange = {
                viewModel.onSignUpPhoneChange(it)
                if (it.length == 10) {
                    keyboardController?.hide()
                }
            },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Phone Icon")
            },
            readOnly = viewModel.isLoading.value,
            isError = viewModel.signUpPhoneError != null || (viewModel.apiError.value != null && viewModel.selectedTab == AuthTab.SIGN_UP),
            supportingText = {
                if (viewModel.signUpPhoneError != null) {
                    Text(viewModel.signUpPhoneError!!, color = MaterialTheme.colorScheme.error)
                }
                if (viewModel.apiError.value != null && viewModel.selectedTab == AuthTab.SIGN_UP) {
                    Text(viewModel.apiError.value!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.onSignUpClicked() // ViewModel now handles navigation
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !viewModel.isLoading.value,
            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
        ) {
            if (viewModel.isLoading.value && viewModel.selectedTab == AuthTab.SIGN_UP) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text("Register", fontSize = 16.sp, color = Color.White)
            }
        }

        // ... (Google Auth UI) ...

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "By clicking start, you agree to our Terms and Conditions",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignInForm(
    viewModel: AuthViewModel,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    Column {
        Text(
            "Login with your phone number",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.signInPhone,
            onValueChange = {
                viewModel.onSignInPhoneChange(it)
                if (it.length == 10) {
                    keyboardController?.hide()
                }
            },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Phone Icon")
            },
            readOnly = viewModel.isLoading.value,
            isError = viewModel.signInPhoneError != null || (viewModel.apiError.value != null && viewModel.selectedTab == AuthTab.SIGN_IN),
            supportingText = {
                if (viewModel.signInPhoneError != null) {
                    Text(viewModel.signInPhoneError!!, color = MaterialTheme.colorScheme.error)
                }
                if (viewModel.apiError.value != null && viewModel.selectedTab == AuthTab.SIGN_IN) {
                    Text(viewModel.apiError.value!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.onSignInClicked() // ViewModel now handles navigation
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !viewModel.isLoading.value,
            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
        ) {
            if (viewModel.isLoading.value && viewModel.selectedTab == AuthTab.SIGN_IN) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text("Next", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}