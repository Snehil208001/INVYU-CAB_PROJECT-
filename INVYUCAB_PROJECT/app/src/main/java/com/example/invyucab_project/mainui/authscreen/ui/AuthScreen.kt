package com.example.invyucab_project.mainui.authscreen.ui

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.authscreen.viewmodel.AuthTab
import com.example.invyucab_project.mainui.authscreen.viewmodel.AuthViewModel
import com.example.invyucab_project.mainui.authscreen.viewmodel.GoogleSignInState
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

// ... (AuthHeader, AuthTabs, AuthTabItem composables are unchanged) ...
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // ✅ ADDED: State for Google Sign-In and a Snackbar host
    val googleSignInState by viewModel.googleSignInState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ✅ ADDED: LaunchedEffect to handle navigation/errors from Google Sign-In
    LaunchedEffect(googleSignInState) {
        when (val state = googleSignInState) {
            is GoogleSignInState.Success -> {
                // Sign-in was successful
                if (state.isNewUser || state.user.displayName.isNullOrBlank()) {
                    // New user or user has no name, go to UserDetails
                    navController.navigate(
                        Screen.UserDetailsScreen.createRoute(
                            phone = state.user.phoneNumber ?: "Google User", // Phone is not available via Google
                            email = state.user.email,
                            name = state.user.displayName
                        )
                    ) {
                        // Clear the auth stack
                        popUpTo(Screen.AuthScreen.route) { inclusive = true }
                    }
                } else {
                    // Existing user, go to Home
                    navController.navigate(Screen.HomeScreen.route) {
                        // Clear the auth stack
                        popUpTo(Screen.AuthScreen.route) { inclusive = true }
                    }
                }
                // Reset the state in the ViewModel
                viewModel.resetGoogleSignInState()
            }
            is GoogleSignInState.Error -> {
                // Show error in a snackbar or toast
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                // Reset the state in the ViewModel
                viewModel.resetGoogleSignInState()
            }
            GoogleSignInState.Loading -> {
                // UI shows its own loading state
            }
            GoogleSignInState.Idle -> {
                // Do nothing
            }
        }
    }


    Scaffold(
        containerColor = CabVeryLightMint,
        // ✅ ADDED: Snackbar host
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
                Column(modifier = Modifier.padding(24.dp)) {
                    AuthTabs(
                        selectedTab = viewModel.selectedTab,
                        onTabSelected = { viewModel.onTabSelected(it) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Crossfade(targetState = viewModel.selectedTab, label = "AuthFormCrossfade") { tab ->
                        when (tab) {
                            // ✅ CHANGED: Pass googleSignInState to SignUpForm
                            AuthTab.SIGN_UP -> SignUpForm(viewModel, navController, googleSignInState)
                            AuthTab.SIGN_IN -> SignInForm(viewModel, navController)
                        }
                    }
                }
            }
        }
    }
}

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
fun AuthTabs(selectedTab: AuthTab, onTabSelected: (AuthTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AuthTabItem(
            text = "Sign Up",
            isSelected = selectedTab == AuthTab.SIGN_UP,
            onClick = { onTabSelected(AuthTab.SIGN_UP) }
        )
        AuthTabItem(
            text = "Sign In",
            isSelected = selectedTab == AuthTab.SIGN_IN,
            onClick = { onTabSelected(AuthTab.SIGN_IN) }
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


@Composable
fun SignUpForm(
    viewModel: AuthViewModel,
    navController: NavController,
    googleSignInState: GoogleSignInState // ✅ ADDED: Pass state for loading
) {
    Column {
        OutlinedTextField(
            value = viewModel.signUpEmail,
            onValueChange = { viewModel.onSignUpEmailChange(it) },
            label = { Text("Email (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email Icon")
            },
            // ✅ ADDED: Show email error
            isError = viewModel.signUpEmailError != null,
            supportingText = {
                if (viewModel.signUpEmailError != null) {
                    Text(viewModel.signUpEmailError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.signUpPhone,
            onValueChange = { viewModel.onSignUpPhoneChange(it) },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Phone Icon")
            },
            // ✅ ADDED: Show phone error
            isError = viewModel.signUpPhoneError != null,
            supportingText = {
                if (viewModel.signUpPhoneError != null) {
                    Text(viewModel.signUpPhoneError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.onSignUpClicked { phone ->
                    // ✅ MODIFIED: Pass 'isSignUp = true'
                    navController.navigate(
                        Screen.OtpScreen.createRoute(
                            phone = phone,
                            isSignUp = true, // <-- THIS IS THE FIX
                            email = viewModel.signUpEmail.takeIf { it.isNotBlank() }
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
        ) {
            Text("Sign Up", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(" OR ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
            Divider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))

        // ✅ MODIFIED: Google Sign-In Button
        OutlinedButton(
            onClick = {
                // Only call if not already loading
                if (googleSignInState != GoogleSignInState.Loading) {
                    viewModel.onGoogleSignInClicked()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            // Disable button while loading
            enabled = googleSignInState != GoogleSignInState.Loading
        ) {
            // Show loading spinner or icon/text
            if (googleSignInState == GoogleSignInState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = CabMintGreen
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connect with Google",
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        }


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

@Composable
fun SignInForm(viewModel: AuthViewModel, navController: NavController) {
    Column {
        Text(
            "Login with your phone number",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.signInPhone,
            onValueChange = { viewModel.onSignInPhoneChange(it) },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Phone Icon")
            },
            // ✅ ADDED: Show phone error
            isError = viewModel.signInPhoneError != null,
            supportingText = {
                if (viewModel.signInPhoneError != null) {
                    Text(viewModel.signInPhoneError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.onSignInClicked { phone ->
                    // ✅ MODIFIED: Pass 'isSignUp = false'
                    navController.navigate(
                        Screen.OtpScreen.createRoute(
                            phone = phone,
                            isSignUp = false, // <-- THIS IS THE FIX
                            email = null // No email on sign in
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
        ) {
            Text("Next", fontSize = 16.sp)
        }
    }
}