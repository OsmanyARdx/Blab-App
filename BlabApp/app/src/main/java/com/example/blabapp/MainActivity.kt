package com.example.blabapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blabapp.ui.theme.BlabAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlabAppTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splashScreen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splashScreen") {
                            SplashScreen(navController = navController)
                        }
                        composable("startupScreen") {
                            StartupScreen(navController = navController)
                        }
                        composable("loginScreen") {
                            LoginScreen(navController = navController)
                        }
                        composable("registrationScreen") {
                            RegistrationScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
