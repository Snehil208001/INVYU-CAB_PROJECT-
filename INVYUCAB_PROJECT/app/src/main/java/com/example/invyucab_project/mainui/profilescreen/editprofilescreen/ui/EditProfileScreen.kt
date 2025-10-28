package com.example.invyucab_project.mainui.profilescreen.editprofilescreen.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen // Import Screen
import com.example.invyucab_project.mainui.profilescreen.editprofilescreen.viewmodel.EditProfileViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.LightSlateGray
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    // === STATE FOR DIALOGS ===
    var showGenderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                title = { Text("My Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape),
                        tint = Color.Gray
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    // --- UPDATED THIS ROW ---
                    ClickableProfileRow(
                        label = "Level",
                        value = "Gold Member",
                        onClick = {
                            navController.navigate(Screen.MemberLevelScreen.route)
                        }
                    )
                }
                item {
                    EditableProfileRow(
                        label = "Name",
                        value = viewModel.name,
                        onValueChange = viewModel::onNameChange,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        )
                    )
                }
                item {
                    EditableProfileRow(
                        label = "Email",
                        value = viewModel.email,
                        onValueChange = viewModel::onEmailChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
                item {
                    ClickableProfileRow(
                        label = "Gender",
                        value = viewModel.gender,
                        onClick = { showGenderDialog = true }
                    )
                }
                item {
                    ClickableProfileRow(
                        label = "Birthday",
                        value = viewModel.birthday,
                        onClick = { datePickerDialog.show() }
                    )
                }
                item {
                    ProfileRow(
                        label = "Phone number",
                        value = viewModel.phone
                    )
                }
            }

            // --- Save Button ---
            Button(
                onClick = {
                    viewModel.onSaveClicked {
                        navController.navigateUp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CabMintGreen)
            ) {
                Text("Save Changes", fontSize = 16.sp, color = Color.White)
            }
        }
    }

    // === GENDER SELECTION DIALOG ===
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
 * An editable row with a BasicTextField.
 */
@Composable
private fun EditableProfileRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val textStyle = TextStyle(
        fontSize = 16.sp,
        color = Color.Black.copy(alpha = 0.7f),
        textAlign = TextAlign.End
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.4f)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(0.6f),
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            cursorBrush = SolidColor(CabMintGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.7f)
        )
    }
    Divider(color = LightSlateGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 24.dp))
}

/**
 * A non-editable row used for display only (e.g., Phone).
 */
@Composable
private fun ProfileRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Gray, // Display-only value is gray
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.7f)
        )
    }
    Divider(color = LightSlateGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 24.dp))
}

/**
 * A row that looks like ProfileRow but is clickable to trigger an action.
 */
@Composable
private fun ClickableProfileRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Clickable
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black.copy(alpha = 0.7f), // Value is not gray
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.7f)
        )
    }
    Divider(color = LightSlateGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 24.dp))
}

/**
 * A dialog for selecting gender.
 */
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
        null // Return null if parsing fails
    }
}

private fun formatDate(calendar: Calendar): String {
    val format = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
    return format.format(calendar.time)
}