package com.example.invyucab_project.mainui.courierscreen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierDetailsScreen(
    navController: NavController,
    pickupPlaceId: String,
    pickupDesc: String,
    dropPlaceId: String,
    dropDesc: String,
    scheduleTime: Long
) {
    var receiverName by remember { mutableStateOf("") }
    var receiverPhone by remember { mutableStateOf("") }
    var packageType by remember { mutableStateOf("") }
    var packageWeight by remember { mutableStateOf("") }

    val commonPackageTypes = listOf("Documents", "Clothes", "Electronics", "Food", "Keys")

    // Basic Validation
    val isFormValid = receiverName.isNotBlank() &&
            receiverPhone.length >= 10 &&
            packageType.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Courier Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5), // Light gray background for contrast
        bottomBar = {
            // Fixed Bottom Button
            ContainerWithShadow {
                Button(
                    onClick = {
                        val route = Screen.RideSelectionScreen.createRoute(
                            dropPlaceId = dropPlaceId,
                            dropDescription = dropDesc,
                            pickupPlaceId = pickupPlaceId,
                            pickupDescription = pickupDesc,
                            isCourier = true,
                            receiverName = receiverName,
                            receiverPhone = receiverPhone,
                            packageType = packageType,
                            scheduleTime = scheduleTime
                        )
                        navController.navigate(route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CabMintGreen,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid
                ) {
                    Text(
                        "Proceed to Vehicle Selection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Info Banner ---
            InfoBanner()

            // --- Section 1: Receiver Details ---
            Text(
                "Receiver Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CourierTextField(
                        value = receiverName,
                        onValueChange = { receiverName = it },
                        label = "Name",
                        icon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CourierTextField(
                        value = receiverPhone,
                        onValueChange = { if (it.length <= 10) receiverPhone = it.filter { char -> char.isDigit() } },
                        label = "Phone Number",
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        placeholder = "9876543210"
                    )
                }
            }

            // --- Section 2: Package Details ---
            Text(
                "Package Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Quick Select Chips
                    Text(
                        "Quick Select:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        commonPackageTypes.forEach { type ->
                            PackageTypeChip(
                                text = type,
                                isSelected = packageType == type,
                                onClick = { packageType = type }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CourierTextField(
                        value = packageType,
                        onValueChange = { packageType = it },
                        label = "Item Description",
                        icon = Icons.Default.Description,
                        placeholder = "e.g. Box of Books"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CourierTextField(
                        value = packageWeight,
                        onValueChange = { packageWeight = it },
                        label = "Approx Weight (kg)",
                        icon = Icons.Default.FitnessCenter,
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            // Bottom Spacer to scroll above button
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- Reusable Components ---

@Composable
fun InfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CabVeryLightMint, RoundedCornerShape(8.dp))
            .border(1.dp, CabMintGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = CabMintGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Ensure items are packed securely. We do not deliver illegal or hazardous items.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
    }
}

@Composable
fun CourierTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = CabMintGreen) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CabMintGreen,
            focusedLabelColor = CabMintGreen,
            cursorColor = CabMintGreen,
            unfocusedBorderColor = Color.LightGray
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun PackageTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) CabMintGreen else Color.White
    val textColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) CabMintGreen else Color.LightGray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ContainerWithShadow(content: @Composable () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}