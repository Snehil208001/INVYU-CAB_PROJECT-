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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.mainui.vehiclepreferences.viewmodel.VehiclePreferencesViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
// ✅ --- START: Corrected Import ---
// ✅ --- END: Corrected Import ---


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

    // ✅ --- START: Corrected 'isLoading' ---
    // 'isLoading' is already a State in BaseViewModel, no collectAsState needed
    val isLoading by viewModel.isLoading
    // ✅ --- END: Corrected 'isLoading' ---

    val apiError by viewModel.apiError
    val snackbarHostState = remember { SnackbarHostState() }

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
                onValueChange = viewModel::onVehicleNumberChange,
                label = "Vehicle Number",
                readOnly = isLoading // This will now work
            )

            VehicleTextField(
                value = model,
                onValueChange = viewModel::onModelChange,
                label = "Model",
                readOnly = isLoading // This will now work
            )

            ExposedDropdownMenuBox(
                expanded = isTypeDropdownExpanded,
                onExpandedChange = { if (!isLoading) viewModel.onSetTypeDropdownExpanded(it) }, // This will now work
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor() // This will now work
                )
                ExposedDropdownMenu(
                    expanded = isTypeDropdownExpanded,
                    onDismissRequest = { viewModel.onSetTypeDropdownExpanded(false) }
                ) {
                    vehicleTypes.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                viewModel.onTypeChange(item)
                            }
                        )
                    }
                }
            }

            VehicleTextField(
                value = color,
                onValueChange = viewModel::onColorChange,
                label = "Color",
                readOnly = isLoading // This will now work
            )

            VehicleTextField(
                value = capacity,
                onValueChange = viewModel::onCapacityChange,
                label = "Capacity",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                readOnly = isLoading // This will now work
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onAddVehicleClicked() },
                enabled = !isLoading, // This will now work
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Add Vehicle", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
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
        readOnly = readOnly
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