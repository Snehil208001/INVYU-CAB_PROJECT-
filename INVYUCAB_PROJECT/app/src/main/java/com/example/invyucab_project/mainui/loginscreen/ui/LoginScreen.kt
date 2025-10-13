package com.example.invyucab_project.mainui.loginscreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.invyucab_project.core.navigations.Screen
import com.example.invyucab_project.mainui.loginscreen.viewmodel.LoginScreenViewModel
import com.example.invyucab_project.ui.theme.*

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginScreenViewModel
) {
    val emailOrPhone = viewModel.emailOrPhone
    val password = viewModel.password
    val isUser = viewModel.isUser
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // ✅ Made screen scrollable
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Login Illustration",
            modifier = Modifier.height(250.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("-------- Login As --------", color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { viewModel.onRoleChange(true) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUser) CabLightGreen else Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = if (!isUser) Color.LightGray else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text("User", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { viewModel.onRoleChange(false) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isUser) CabLightGreen else Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = if (isUser) Color.LightGray else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text("Driver", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFF7))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { viewModel.onEmailOrPhoneChange(it) },
                    label = { Text("Email or Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next // ✅ Set IME action to Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) } // ✅ Move focus down
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Email or Phone Icon"
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done // ✅ Set IME action to Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() } // ✅ Clear focus (hide keyboard)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon"
                        )
                    },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle Password")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { /* Handle forgot password */ },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            "Forgot Password?",
                            color = LinkColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val annotatedText = buildAnnotatedString {
            append("Don't have a ${if (isUser) "User" else "Driver"} account? ")
            pushStringAnnotation(tag = "REGISTER", annotation = "REGISTER")
            withStyle(style = SpanStyle(color = LinkColor, fontWeight = FontWeight.Bold)) {
                append("Register Now")
            }
            pop()
        }
        ClickableText(
            text = annotatedText,
            onClick = {
                if (isUser) {
                    navController.navigate(Screen.UserSignUpScreen.route)
                } else {
                    navController.navigate(Screen.DriverSignUpScreen.route)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.onLoginClicked() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = CabPrimaryGreen)
        ) {
            Text("Login", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}