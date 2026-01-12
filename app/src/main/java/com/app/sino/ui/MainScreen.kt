package com.app.sino.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.sino.ui.components.BottomNavigationBar
import com.app.sino.ui.navigation.Screen
import com.app.sino.ui.screens.CalendarScreen
import com.app.sino.ui.screens.CoursesScreen
import com.app.sino.ui.screens.HomeScreen
import com.app.sino.ui.screens.PathScreen
import com.app.sino.ui.screens.ProfileScreen

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Path.route) { PathScreen() }
            composable(Screen.Courses.route) { CoursesScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Profile.route) { ProfileScreen(onLogout = onLogout) }
        }
    }
}