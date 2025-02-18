package com.example.blabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import kotlinx.coroutines.delay

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow


@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(BlabPurple),
        contentAlignment = Alignment.Center
    ) {
        Text("Blab App", fontSize = 50.sp, color = BlabYellow)
    }

    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("StartupScreen") // After splash screen, navigate to startup
    }
}
