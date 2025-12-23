package com.app.sino.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.app.sino.data.util.Resource
import com.app.sino.ui.auth.AuthViewModel
import com.app.sino.ui.auth.ForgotPasswordScreen
import com.app.sino.ui.auth.LoginScreen
import com.app.sino.ui.auth.SignUpScreen
import com.app.sino.ui.auth.WelcomeScreen
import com.app.sino.ui.navigation.AuthScreen

@Composable
fun RootScreen(
    viewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState = viewModel.authState.collectAsState().value

    if (authState is Resource.Loading && authState.data == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val startDestination = if (authState is Resource.Success) "main_graph" else "auth_graph"
        
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            navigation(
                startDestination = AuthScreen.Welcome.route,
                route = "auth_graph"
            ) {
                composable(AuthScreen.Welcome.route) {
                    WelcomeScreen(
                        onLoginClick = { navController.navigate(AuthScreen.Login.route) },
                        onSignUpClick = { navController.navigate(AuthScreen.SignUp.route) },
                        onGoogleClick = { 
                           // Google sign in placeholder
                        }
                    )
                }
                composable(AuthScreen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("main_graph") {
                                popUpTo("auth_graph") { inclusive = true }
                            }
                        },
                        onForgotPasswordClick = { navController.navigate(AuthScreen.ForgotPassword.route) },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(AuthScreen.SignUp.route) {
                    SignUpScreen(
                        onSignUpSuccess = {
                            navController.navigate("main_graph") {
                                popUpTo("auth_graph") { inclusive = true }
                            }
                        }
                    )
                }
                composable(AuthScreen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onResetClick = {
                            navController.navigate(AuthScreen.Login.route) {
                                popUpTo(AuthScreen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("main_graph") {
                MainScreen(onLogout = {
                    viewModel.logout()
                    navController.navigate("auth_graph") {
                        popUpTo("main_graph") { inclusive = true }
                    }
                })
            }
        }
    }
}
