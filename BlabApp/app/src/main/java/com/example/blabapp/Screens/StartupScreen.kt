package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.blabapp.R
import com.example.blabapp.ui.theme.BlabBlue
import com.example.blabapp.ui.theme.BlabGreen
import com.example.blabapp.ui.theme.BlabGrey
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow


@Composable
fun StartupScreen(navController: NavController) {
    val logoPic = painterResource(R.drawable.logo)
    Box(
        modifier = Modifier.fillMaxSize().background(BlabPurple),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = logoPic,
                contentDescription = null
            )
            Text("Welcome to Blab App", fontSize = 30.sp, color = BlabYellow, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = { navController.navigate("LoginScreen") },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = BlabGreen, contentColor = BlabYellow)
            ) {
                Text(text = "Login", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("RegisterScreen") },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = BlabBlue, contentColor = BlabYellow)
            ) {
                Text(text = "Register", fontSize = 20.sp)
            }
        }
    }
}
