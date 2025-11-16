package com.example.invyucab_project.mainui.userdetailsscreen.ui


import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cake
// import androidx.compose.material.icons.filled.Email // ❌ REMOVED
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.base.BaseViewModel
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.userdetailsscreen.viewmodel.UserDetailsViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
// import com.example.invyucab_project.ui.theme.LightSlateGray // ❌ REMOVED (if unused)
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserDetailsScreen(
    navController: NavController,
    viewModel: UserDetailsViewModel = hiltViewModel()
) {
    var showGenderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val calendar = remember {
        parseDate(viewModel.birthday) ?: Calendar.getInstance()
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
            viewModel.onBirthdayChange(formatDate(selectedCalendar))
        }, year, month, day
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BaseViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }
                is BaseViewModel.UiEvent.ShowSnackbar -> {
                    // Handle snackbar if needed
                }
                // ✅ --- THIS IS THE FIX ---
                // Add an else branch to make the 'when' exhaustive
                else -> {}
                // ✅ --- END OF FIX ---
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
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
                "Just one last step!",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                "Let's get to know you better.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Full Name Field
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words
                ),
                shape = RoundedCornerShape(8.dp),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Name Icon")
                },
                singleLine = true,
                isError = viewModel.nameError != null,
                supportingText = {
                    if (viewModel.nameError != null) {
                        Text(viewModel.nameError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ❌❌❌ EMAIL FIELD REMOVED ❌❌❌
            /*
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email(Optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(8.dp),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email Icon")
                },
                singleLine = true,
                enabled = !viewModel.isEmailFromGoogle,
                isError = viewModel.emailError != null,
                supportingText = {
                    if (viewModel.emailError != null) {
                        Text(viewModel.emailError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    disabledTextColor = Color.Black.copy(alpha = 0.8f),
                    disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                    disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                    disabledLabelColor = Color.Gray,
                    disabledContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            */
            // ❌❌❌ END OF REMOVAL ❌❌❌


            // Phone Field
            OutlinedTextField(
                value = viewModel.phone,
                onValueChange = {
                    viewModel.onPhoneChange(it)
                    if (it.length == 10) {
                        keyboardController?.hide()
                    }
                },
                label = { Text("Mobile Number *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Phone Icon")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !viewModel.isPhoneFromMobileAuth,
                isError = viewModel.phoneError != null,
                supportingText = {
                    if (viewModel.phoneError != null) {
                        Text(viewModel.phoneError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    disabledTextColor = Color.Black.copy(alpha = 0.8f),
                    disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                    disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                    disabledLabelColor = Color.Gray,
                    disabledContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Field (Clickable)
            ClickableOutlinedTextField(
                value = viewModel.gender.ifEmpty { "Gender" }, // Use placeholder
                label = "Gender *",
                leadingIcon = Icons.Default.Wc,
                onClick = { showGenderDialog = true },
                isPlaceholder = viewModel.gender.isEmpty() // Pass placeholder status
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DOB Field (Clickable)
            ClickableOutlinedTextField(
                value = viewModel.birthday.ifEmpty { "Date of Birth" },
                label = "Date of Birth *",
                leadingIcon = Icons.Default.Cake,
                onClick = { datePickerDialog.show() },
                isError = viewModel.birthdayError != null,
                isPlaceholder = viewModel.birthday.isEmpty(), // Pass placeholder status
                supportingText = {
                    if (viewModel.birthdayError != null) {
                        Text(viewModel.birthdayError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.onSaveClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                enabled = viewModel.name.isNotBlank() &&
                        (viewModel.isPhoneFromMobileAuth || viewModel.phone.length == 10) &&
                        viewModel.birthday.isNotBlank() &&
                        viewModel.gender.isNotBlank()
            ) {
                Text("Save & Continue", fontSize = 16.sp, color = Color.White)
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
                disabledContainerColor = Color.White
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
        val format = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
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
    val format = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
    return format.format(calendar.time)
}