package com.example.invyucab_project.mainui.driverdetailsscreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel.DriverDetailsViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailsScreen(
    navController: NavController,
    viewModel: DriverDetailsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ ADDED: Show snackbar on API error
    LaunchedEffect(viewModel.apiError) {
        viewModel.apiError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = CabVeryLightMint,
        snackbarHost = { SnackbarHost(snackbarHostState) }, // ✅ ADDED
        topBar = {
            TopAppBar(
                title = { Text("Driver Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        // ✅ ADDED: Box for loading indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Please provide your details to register as a driver.",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                // 5.3.3: Auto-filled fields
                item {
                    OutlinedTextField(
                        value = viewModel.name,
                        onValueChange = {},
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black.copy(alpha = 0.8f),
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                            disabledLabelColor = Color.Gray
                        )
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black.copy(alpha = 0.8f),
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                            disabledLabelColor = Color.Gray
                        )
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.phone,
                        onValueChange = {},
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black.copy(alpha = 0.8f),
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                            disabledLabelColor = Color.Gray
                        )
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.gender,
                        onValueChange = {},
                        label = { Text("Gender") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black.copy(alpha = 0.8f),
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                            disabledLabelColor = Color.Gray
                        )
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.rawDob, // ✅ Show raw DOB
                        onValueChange = {},
                        label = { Text("Date of Birth") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Black.copy(alpha = 0.8f),
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                            disabledLabelColor = Color.Gray
                        )
                    )
                }

                item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

                // 5.3.4: New driver-specific fields
                item {
                    OutlinedTextField(
                        value = viewModel.aadhaarNumber,
                        onValueChange = viewModel::onAadhaarChange,
                        label = { Text("Aadhaar Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        readOnly = viewModel.isLoading, // ✅ ADDED
                        isError = viewModel.aadhaarError != null,
                        supportingText = {
                            if (viewModel.aadhaarError != null) {
                                Text(viewModel.aadhaarError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White
                        )
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.licenceNumber,
                        onValueChange = viewModel::onLicenceChange,
                        label = { Text("Licence Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        readOnly = viewModel.isLoading, // ✅ ADDED
                        isError = viewModel.licenceError != null,
                        supportingText = {
                            if (viewModel.licenceError != null) {
                                Text(viewModel.licenceError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White
                        )
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.vehicleNumber,
                        onValueChange = viewModel::onVehicleChange,
                        label = { Text("Vehicle Number (e.g., BR01AB1234)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        readOnly = viewModel.isLoading, // ✅ ADDED
                        isError = viewModel.vehicleError != null,
                        supportingText = {
                            if (viewModel.vehicleError != null) {
                                Text(viewModel.vehicleError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White
                        )
                    )
                }

                // 5.3.5: Submit Button
                item {
                    Button(
                        onClick = {
                            viewModel.onSubmitClicked {
                                // 5.3.6: Navigate to DriverScreen
                                navController.navigate(Screen.DriverScreen.route) {
                                    // 5.3.7: Registration is complete, clear auth stack
                                    popUpTo(Screen.AuthScreen.route) { inclusive = true }
                                }
                            }
                        },
                        enabled = !viewModel.isLoading, // ✅ ADDED
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
                    ) {
                        Text("Submit", fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            // ✅ ADDED: Loading Indicator
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = CabMintGreen)
            }
        }
    }
}