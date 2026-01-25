package com.app.sino.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.sino.R
import com.app.sino.ui.components.BottomNavigationBar
import com.app.sino.ui.navigation.Screen
import com.app.sino.ui.screens.AddStudyPlanScreen
import com.app.sino.ui.screens.CalendarScreen
import com.app.sino.ui.screens.CoursesScreen
import com.app.sino.ui.screens.HomeScreen
import com.app.sino.ui.screens.PathScreen
import com.app.sino.ui.screens.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavScreens = listOf(
        Screen.Home, Screen.Path, Screen.Courses, Screen.Calendar, Screen.Profile
    )

    val currentScreen = bottomNavScreens.find { it.route == currentRoute }
        ?: if (currentRoute == Screen.AddStudyPlan.route) Screen.AddStudyPlan else Screen.Home

    Scaffold(
        topBar = {
            if (currentRoute != Screen.AddStudyPlan.route) {
                TopAppBar(
                    title = { 
                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineSmall
                            ) 
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.Profile) {
                            IconButton(onClick = { /* Search action */ }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.magnifying_glass),
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = { 

            if (bottomNavScreens.any { it.route == currentRoute }) {
                BottomNavigationBar(navController = navController)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(if (currentRoute == Screen.AddStudyPlan.route) PaddingValues(0.dp) else innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Path.route) { PathScreen() }
            composable(Screen.Courses.route) { 
                CoursesScreen(
                    onAddPlanClick = { navController.navigate(Screen.AddStudyPlan.route) }
                ) 
            }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Profile.route) { ProfileScreen(onLogout = onLogout) }
            
            composable(Screen.AddStudyPlan.route) { 
                AddStudyPlanScreen(
                    onBack = { navController.popBackStack() }
                ) 
            }
        }
    }
}