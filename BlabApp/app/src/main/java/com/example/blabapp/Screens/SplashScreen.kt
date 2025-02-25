package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import kotlinx.coroutines.delay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.blabapp.R
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow


@Composable
fun SplashScreen(navController: NavController) {
    val logoPic = painterResource(R.drawable.logo)

    Box(
        modifier = Modifier.fillMaxSize().background(BlabYellow),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(100.dp))
                .padding(16.dp)
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = logoPic,
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("StartupScreen")
    }
}
