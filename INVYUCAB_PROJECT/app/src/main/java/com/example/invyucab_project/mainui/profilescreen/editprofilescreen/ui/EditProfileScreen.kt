package com.example.invyucab_project.mainui.profilescreen.editprofilescreen.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.profilescreen.editprofilescreen.viewmodel.EditProfileViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.LightSlateGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // === LISTEN FOR UI EVENTS (TOASTS) ===
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Details", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Level can still be clickable for navigation
                item {
                    ClickableProfileRow(
                        label = "Level",
                        value = "Gold Member",
                        onClick = {
                            navController.navigate(Screen.MemberLevelScreen.route)
                        }
                    )
                }

                // Name - Read Only
                item {
                    ProfileRow(
                        label = "Name",
                        value = viewModel.name
                    )
                }

                // Gender - Read Only
                item {
                    ProfileRow(
                        label = "Gender",
                        value = viewModel.gender
                    )
                }

                // Birthday - Read Only
                item {
                    ProfileRow(
                        label = "Birthday",
                        value = viewModel.birthday.ifEmpty { "Not Available" }
                    )
                }

                // Phone number - Read Only
                item {
                    ProfileRow(
                        label = "Phone number",
                        value = viewModel.phone
                    )
                }
            }

            // === LOADING INDICATOR ===
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CabMintGreen
                )
            }
        }
    }
}

/**
 * A non-editable row used for display only.
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
            color = Color.Gray,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Icon removed or kept gray to indicate non-interactivity
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.3f) // Faded arrow to show it's static
        )
    }
    Divider(color = LightSlateGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 24.dp))
}

/**
 * A row that is clickable to trigger an action (like Navigation).
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
            .clickable(onClick = onClick)
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
            color = Color.Black.copy(alpha = 0.7f),
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