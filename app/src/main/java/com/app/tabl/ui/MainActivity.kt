package com.app.tabl.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.app.tabl.ui.navigation.TablNavGraph
import com.app.tabl.ui.theme.TablTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by mainViewModel.theme.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (theme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            TablTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                TablNavGraph(navController = navController)
            }
        }
    }
}
