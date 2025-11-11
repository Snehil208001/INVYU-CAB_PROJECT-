package com.example.invyucab_project.mainui.driverdetailsscreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons // ✅ ADDED Import
import androidx.compose.material.icons.automirrored.filled.ArrowBack // ✅ ADDED Import
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
import com.example.invyucab_project.core.base.BaseViewModel
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
    val isLoading = viewModel.isLoading.value
    val apiError = viewModel.apiError.value

    // --- Event Collection ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    // ✅ THIS IS THE FIX
                    navController.navigate(event.route)
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // --- Error Handling ---
    LaunchedEffect(apiError) {
        if (apiError != null) {
            snackbarHostState.showSnackbar(
                message = apiError,
                duration = SnackbarDuration.Short
            )
            viewModel.clearApiError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Driver Details") },
                // ✅✅✅ START OF FIX ✅✅✅
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                // ✅✅✅ END OF FIX ✅✅✅
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White // This ensures the icon is white
                )
            )
        },
        containerColor = CabVeryLightMint

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Almost there! Just a few more details.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // --- Form Fields ---
                DriverTextField(
                    value = viewModel.aadhaarNumber,
                    onValueChange = viewModel::onAadhaarChange,
                    label = "Aadhaar Number",
                    keyboardType = KeyboardType.Number,
                    isError = viewModel.aadhaarError != null,
                    errorText = viewModel.aadhaarError,
                    readOnly = isLoading
                )
                DriverTextField(
                    value = viewModel.licenceNumber,
                    onValueChange = viewModel::onLicenceChange,
                    label = "Driving Licence Number",
                    capitalization = KeyboardCapitalization.Characters,
                    isError = viewModel.licenceError != null,
                    errorText = viewModel.licenceError,
                    readOnly = isLoading
                )
                DriverTextField(
                    value = viewModel.vehicleNumber,
                    onValueChange = viewModel::onVehicleChange,
                    label = "Vehicle Registration Number",
                    capitalization = KeyboardCapitalization.Characters,
                    isError = viewModel.vehicleError != null,
                    errorText = viewModel.vehicleError,
                    readOnly = isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.onSubmitClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("Submit & Start Driving", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isError: Boolean,
    errorText: String?,
    readOnly: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization
        ),
        singleLine = true,
        readOnly = readOnly,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(errorText ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}