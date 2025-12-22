package com.app.sino.ui.navigation

sealed class AuthScreen(val route: String) {
    data object Welcome : AuthScreen("welcome")
    data object Login : AuthScreen("login")
    data object SignUp : AuthScreen("signup")
    data object ForgotPassword : AuthScreen("forgot_password")
}
