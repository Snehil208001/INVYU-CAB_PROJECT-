package com.example.invyucab_project.mainui.vehiclepreferences.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// ✅ --- IMPORT ADDED FOR THE FIX ---
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.mainui.vehiclepreferences.viewmodel.VehiclePreferencesViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclePreferencesScreen(
    navController: NavController,
    viewModel: VehiclePreferencesViewModel = hiltViewModel()
) {
    val vehicleNumber by viewModel.vehicleNumber.collectAsState()
    val model by viewModel.model.collectAsState()
    val type by viewModel.type.collectAsState()
    val color by viewModel.color.collectAsState()
    val capacity by viewModel.capacity.collectAsState()

    val isTypeDropdownExpanded by viewModel.isTypeDropdownExpanded.collectAsState()
    val vehicleTypes = viewModel.vehicleTypes

    val isEditMode by viewModel.isEditMode.collectAsState()
    val isLoading by viewModel.isLoading
    val apiError by viewModel.apiError
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ --- GET THE FOCUS MANAGER ---
    val focusManager = LocalFocusManager.current

    // --- Event Collection for Navigation/Snackbars ---
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    // --- Error Handling ---
    LaunchedEffect(apiError) {
        if (apiError != null) {
            snackbarHostState.showSnackbar(
                message = apiError!!,
                duration = SnackbarDuration.Short
            )
            viewModel.clearApiError()
        }
    }

    Scaffold(
        topBar = {
            VehiclePreferencesTopAppBar(
                onBackClicked = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            VehicleTextField(
                value = vehicleNumber,
                onValueChange = {}, // Disabled editing
                label = "Vehicle Number",
                readOnly = true // Set to true
            )

            VehicleTextField(
                value = model,
                onValueChange = {}, // Disabled editing
                label = "Model",
                readOnly = true // Set to true
            )

            // Replaced Dropdown with a ReadOnly TextField for Type
            VehicleTextField(
                value = type,
                onValueChange = {},
                label = "Type",
                readOnly = true
            )

            VehicleTextField(
                value = color,
                onValueChange = {}, // Disabled editing
                label = "Color",
                readOnly = true // Set to true
            )

            VehicleTextField(
                value = capacity,
                onValueChange = {}, // Disabled editing
                label = "Capacity",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                readOnly = true // Set to true
            )

            Spacer(modifier = Modifier.weight(1f))

        }
    }
}

@Composable
private fun VehicleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        readOnly = readOnly,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
            disabledTextColor = if (readOnly) Color.Gray.copy(alpha = 0.8f) else Color.Black,
            disabledLabelColor = Color.Gray.copy(alpha = 0.5f)
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehiclePreferencesTopAppBar(
    onBackClicked: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Vehicle Preferences",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CabMintGreen,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}