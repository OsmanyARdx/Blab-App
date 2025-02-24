package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import kotlinx.coroutines.delay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blabapp.R
import com.example.blabapp.ui.theme.BlabPurple


@Composable
fun SplashScreen(navController: NavController) {
    val logoPic = painterResource(R.drawable.logo2)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlabPurple),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = logoPic,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }



        Text(
            text = "Blab app",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }






        LaunchedEffect(Unit) {
            delay(2000)
            navController.navigate("StartupScreen")  //navigate to startup screen
        }
    }
