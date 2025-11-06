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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi // ✅ ADDED Import
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController // ✅ ADDED Import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen // Make sure Screen is imported
import com.example.invyucab_project.mainui.userdetailsscreen.viewmodel.UserDetailsViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import com.example.invyucab_project.ui.theme.LightSlateGray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class) // ✅ ADDED ExperimentalComposeUiApi
@Composable
fun UserDetailsScreen(
    navController: NavController,
    viewModel: UserDetailsViewModel = hiltViewModel()
) {
    // === STATE FOR DIALOGS ===
    var showGenderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current // ✅ ADDED Keyboard Controller

    // === DATE PICKER LOGIC ===
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
    // ==========================

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Your Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.AuthScreen.route) {
                            popUpTo(Screen.UserDetailsScreen.route) { inclusive = true }
                            launchSingleTop = true
                        }
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

            // Email Field
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

            // Phone Field
            OutlinedTextField(
                value = viewModel.phone,
                onValueChange = {
                    // ✅ ADDED keyboard hiding logic
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
                value = viewModel.gender,
                label = "Gender *",
                leadingIcon = Icons.Default.Wc,
                onClick = { showGenderDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DOB Field (Clickable)
            ClickableOutlinedTextField(
                value = viewModel.birthday.ifEmpty { "Date of Birth" },
                label = "Date of Birth *",
                leadingIcon = Icons.Default.Cake,
                onClick = { datePickerDialog.show() },
                isError = viewModel.birthdayError != null,
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
                    viewModel.onSaveClicked { phone, email, name, gender, dob ->
                        navController.navigate(
                            Screen.OtpScreen.createRoute(
                                phone = phone,
                                isSignUp = true,
                                email = email,
                                name = name,
                                gender = gender,
                                dob = dob
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen),
                enabled = viewModel.name.isNotBlank() &&
                        (viewModel.isPhoneFromMobileAuth || viewModel.phone.length == 10) &&
                        viewModel.birthday.isNotBlank()
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

/**
 * ✅ THIS IS THE FIX
 * The `onClick` parameter is now passed to the `.clickable()` modifier,
 * which removes the warning you saw.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClickableOutlinedTextField(
    value: String,
    label: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit, // <-- 1. Parameter is received here
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick), // <-- 2. Parameter is USED here
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
                // Handle placeholder color for DOB field
                disabledTextColor = if (value == "Date of Birth") Color.Gray else Color.Black.copy(alpha = 0.8f),
                disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                disabledLeadingIconColor = Color.Black.copy(alpha = 0.8f),
                disabledTrailingIconColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledContainerColor = Color.White,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        if (supportingText != null) {
            Box(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                supportingText()
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
