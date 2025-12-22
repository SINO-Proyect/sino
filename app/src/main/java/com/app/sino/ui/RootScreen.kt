package com.app.sino.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.app.sino.ui.auth.ForgotPasswordScreen
import com.app.sino.ui.auth.LoginScreen
import com.app.sino.ui.auth.SignUpScreen
import com.app.sino.ui.auth.WelcomeScreen
import com.app.sino.ui.navigation.AuthScreen

@Composable
fun RootScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "auth_graph",
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
                        // For now, treat Google sign in as success -> go to main app
                        navController.navigate("main_graph") {
                            popUpTo("auth_graph") { inclusive = true }
                        }
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
                        // For demo, go back to login after "reset"
                        navController.navigate(AuthScreen.Login.route) {
                            popUpTo(AuthScreen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("main_graph") {
            MainScreen()
        }
    }
}
