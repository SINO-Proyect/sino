package com.app.sino.ui.navigation

import androidx.annotation.DrawableRes
import com.app.sino.R

sealed class Screen(
    val route: String, 
    val title: String, 
    @DrawableRes val iconOutline: Int,
    @DrawableRes val iconFilled: Int
) {
    data object Home : Screen("home", "Home", R.drawable.ic_home_outline, R.drawable.ic_home_filled)
    data object Path : Screen("path", "Path", R.drawable.ic_path_outline, R.drawable.ic_path_filled)
    data object Courses : Screen("courses", "Courses", R.drawable.ic_courses_outline, R.drawable.ic_courses_filled)
    data object Calendar : Screen("calendar", "Calendar", R.drawable.ic_calendar_outline, R.drawable.ic_calendar_filled)
    data object Profile : Screen("profile", "Profile", R.drawable.ic_profile_outline, R.drawable.ic_profile_filled)
}
