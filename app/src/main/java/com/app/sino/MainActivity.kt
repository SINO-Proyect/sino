package com.app.sino

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import com.app.sino.ui.RootScreen
import com.app.sino.ui.theme.SINOTheme
import com.app.sino.data.remote.RetrofitClient

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            SINOTheme(darkTheme = systemDark) {
                RootScreen()
            }
        }
    }
}