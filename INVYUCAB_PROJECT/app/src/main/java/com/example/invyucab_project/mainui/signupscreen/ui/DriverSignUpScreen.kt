package com.example.invyucab_project.mainui.signupscreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.invyucab_project.R
import com.example.invyucab_project.mainui.signupscreen.viewmodel.SignUpScreenViewModel
import com.example.invyucab_project.ui.theme.*

@Composable
fun DriverSignUpScreen(
    navController: NavController,
    viewModel: SignUpScreenViewModel
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Sign Up Illustration",
            modifier = Modifier.height(180.dp)
        )

        Text(
            "Create Driver Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Personal Details Card
        Text("Personal Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFF7))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = viewModel.fullName, onValueChange = { viewModel.onFullNameChange(it) }, label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.phone, onValueChange = { viewModel.onPhoneChange(it) }, label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.email, onValueChange = { viewModel.onEmailChange(it) }, label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    leadingIcon = { Icon(Icons.Default.Email, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.password, onValueChange = { viewModel.onPasswordChange(it) }, label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(), visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Vehicle & License Details Card
        Text("Vehicle & License Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFF7))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = viewModel.vehicleModel, onValueChange = { viewModel.onVehicleModelChange(it) }, label = { Text("Vehicle Model (e.g., Swift Dzire)") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.vehicleNumber, onValueChange = { viewModel.onVehicleNumberChange(it) }, label = { Text("Vehicle Number (e.g., BR01 AA 1234)") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    leadingIcon = { Icon(Icons.Default.Pin, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.licenseNumber,
                    onValueChange = { viewModel.onLicenseNumberChange(it) },
                    label = { Text("Driving License Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onDriverSignUpClicked() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = CabPrimaryGreen)
        ) {
            Text("Sign Up as Driver", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        val annotatedText = buildAnnotatedString {
            append("Already have an account? ")
            pushStringAnnotation(tag = "LOGIN", annotation = "LOGIN")
            withStyle(style = SpanStyle(color = LinkColor, fontWeight = FontWeight.Bold)) {
                append("Login")
            }
            pop()
        }
        ClickableText(
            text = annotatedText,
            onClick = { navController.navigateUp() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}