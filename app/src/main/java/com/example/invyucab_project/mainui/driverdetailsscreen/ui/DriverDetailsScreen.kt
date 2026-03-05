package com.example.invyucab_project.mainui.driverdetailsscreen.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.mainui.driverdetailsscreen.viewmodel.DriverDetailsViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailsScreen(
    navController: NavController,
    viewModel: DriverDetailsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = viewModel.isLoading.value
    val apiError = viewModel.apiError.value

    // State for dialogs
    var showGenderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val calendar = remember(viewModel.dob) {
        parseDate(viewModel.dob) ?: Calendar.getInstance()
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            viewModel.onDobChange(formatDate(selectedCalendar))
        }, year, month, day
    )

    // --- Event Collection ---
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
                // Add an else branch to make the 'when' exhaustive
                else -> {}
                // ✅ --- END OF FIX ---
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
                title = { Text("Driver Details") }, // MODIFIED
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
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

                // --- Personal Details ---
                Text(
                    "Personal Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                DriverTextField(
                    value = viewModel.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Full Name *",
                    capitalization = KeyboardCapitalization.Words,
                    isError = viewModel.nameError != null,
                    errorText = viewModel.nameError,
                    readOnly = isLoading,
                    leadingIcon = Icons.Default.Person
                )
                ClickableOutlinedTextField(
                    value = viewModel.gender.ifEmpty { "Gender" },
                    label = "Gender *",
                    leadingIcon = Icons.Default.Wc,
                    onClick = { showGenderDialog = true },
                    isPlaceholder = viewModel.gender.isEmpty(),
                    isError = viewModel.genderError != null,
                    supportingText = {
                        if (viewModel.genderError != null) {
                            Text(viewModel.genderError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                ClickableOutlinedTextField(
                    value = viewModel.dob.ifEmpty { "Date of Birth" },
                    label = "Date of Birth *",
                    leadingIcon = Icons.Default.Cake,
                    onClick = { datePickerDialog.show() },
                    isError = viewModel.dobError != null,
                    isPlaceholder = viewModel.dob.isEmpty(),
                    supportingText = {
                        if (viewModel.dobError != null) {
                            Text(viewModel.dobError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Driver Details ---
                Text(
                    "Driver Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                // REMOVED: Aadhaar Number
                DriverTextField(
                    value = viewModel.licenceNumber,
                    onValueChange = viewModel::onLicenceChange,
                    label = "Driving Licence Number",
                    capitalization = KeyboardCapitalization.Characters,
                    isError = viewModel.licenceError != null,
                    errorText = viewModel.licenceError,
                    readOnly = isLoading
                )

                // REMOVED: Vehicle Details Section

                Spacer(modifier = Modifier.height(32.dp))

                // --- Submit Button ---
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
                        Text("Submit Details", fontSize = 16.sp, color = Color.White) // MODIFIED
                    }
                }
            }
        }
    }

    if (showGenderDialog) {
        GenderSelectionDialog(
            currentGender = viewModel.gender,
            onGenderSelected = {
                viewModel.onGenderChange(it)
                showGenderDialog = false
            },
            onDismiss = { showGenderDialog = false }
        )
    }
}

// --- Composable Helpers (DriverTextField, VehicleTypeDropdown, etc.) ---

@Composable
fun DriverTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isError: Boolean,
    errorText: String?,
    readOnly: Boolean,
    leadingIcon: ImageVector? = null
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
            if (isError && errorText != null) {
                Text(errorText, color = MaterialTheme.colorScheme.error)
            }
        },
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null)
            }
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White
        )
    )
}

// REMOVED: VehicleTypeDropdown Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClickableOutlinedTextField(
    value: String,
    label: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    isError: Boolean = false,
    isPlaceholder: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null)
            },
            trailingIcon = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
            singleLine = true,
            enabled = false,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = if (isPlaceholder) Color.Gray else Color.Black.copy(alpha = 0.8f),
                disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                disabledTrailingIconColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White
            )
        )
        if (supportingText != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val textColor = if (isError) MaterialTheme.colorScheme.error else Color.Gray
                ProvideTextStyle(
                    value = MaterialTheme.typography.bodySmall.copy(color = textColor),
                    content = supportingText
                )
            }
        }
    }
}


@Composable
private fun GenderSelectionDialog(
    currentGender: String,
    onGenderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val genderOptions = listOf("Male", "Female", "Prefer not to say")
    var selectedOption by remember { mutableStateOf(currentGender) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Gender") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                genderOptions.forEach { gender ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (gender == selectedOption),
                                onClick = { selectedOption = gender }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (gender == selectedOption),
                            onClick = { selectedOption = gender },
                            colors = RadioButtonDefaults.colors(selectedColor = CabMintGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = gender)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGenderSelected(selectedOption) },
                colors = ButtonDefaults.textButtonColors(contentColor = CabMintGreen)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}

// === DATE HELPER FUNCTIONS ===

private fun parseDate(dateString: String): Calendar? {
    return try {
        // MODIFIED: Updated date format to match API
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = format.parse(dateString)
        Calendar.getInstance().apply {
            if (date != null) {
                time = date
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun formatDate(calendar: Calendar): String {
    // MODIFIED: Updated date format to match API
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return format.format(calendar.time)
}