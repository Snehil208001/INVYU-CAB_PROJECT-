package com.example.invyucab_project.mainui.authscreen.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

// ... (AuthScreen, AuthHeader, AuthTabs, AuthTabItem composables are unchanged) ...
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    Scaffold(containerColor = CabVeryLightMint) { padding ->
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
                            // ✅ CHANGED: Pass navController to both forms
                            AuthTab.SIGN_UP -> SignUpForm(viewModel, navController)
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
fun SignUpForm(viewModel: AuthViewModel, navController: NavController) {
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
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.onSignUpClicked { phone ->
                    // ✅ MODIFIED: Pass both phone and the optional email
                    navController.navigate(
                        Screen.OtpScreen.createRoute(
                            phone,
                            viewModel.signUpEmail.takeIf { it.isNotBlank() } // Pass email only if not blank
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
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

        OutlinedButton(
            onClick = { /* TODO: Handle Google Login */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
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
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.onSignInClicked { phone ->
                    // ✅ MODIFIED: Pass phone and a null email
                    navController.navigate(Screen.OtpScreen.createRoute(phone, null))
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
        ) {
            Text("Next", fontSize = 16.sp)
        }
    }
}