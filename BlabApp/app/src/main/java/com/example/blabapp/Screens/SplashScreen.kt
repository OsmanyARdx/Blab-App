package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import kotlinx.coroutines.delay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.blabapp.R
import com.example.blabapp.ui.theme.BlabPurple


@Composable
fun SplashScreen(navController: NavController) {
    val logoPic = painterResource(R.drawable.logo)
    Box(
        modifier = Modifier.fillMaxSize().background(BlabPurple),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = logoPic,
            contentDescription = null
        )
    }

    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("StartupScreen") // After splash screen, navigate to startup
    }
}
