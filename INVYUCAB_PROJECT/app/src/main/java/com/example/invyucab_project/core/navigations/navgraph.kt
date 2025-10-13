package com.example.invyucab_project.core.navigations

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.invyucab_project.mainui.loginscreen.ui.LoginScreen
import com.example.invyucab_project.mainui.loginscreen.viewmodel.LoginScreenViewModel
import com.example.invyucab_project.mainui.signupscreen.ui.DriverSignUpScreen
import com.example.invyucab_project.mainui.signupscreen.ui.UserSignUpScreen
import com.example.invyucab_project.mainui.signupscreen.viewmodel.SignUpScreenViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route
    ) {
        composable(Screen.LoginScreen.route) {
            val viewModel: LoginScreenViewModel = hiltViewModel()
            LoginScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.UserSignUpScreen.route) {
            val viewModel: SignUpScreenViewModel = hiltViewModel()
            UserSignUpScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.DriverSignUpScreen.route) {
            val viewModel: SignUpScreenViewModel = hiltViewModel()
            DriverSignUpScreen(navController = navController, viewModel = viewModel)
        }
    }
}