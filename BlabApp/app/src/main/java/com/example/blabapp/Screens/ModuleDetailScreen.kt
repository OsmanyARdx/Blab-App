package com.example.blabapp.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun ModuleDetailScreen(navController: NavHostController, moduleId: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    navController.navigate("lesson/$moduleId") {
                        popUpTo("moduleDetails/$moduleId") { inclusive = true }
                    }
                },
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onTertiary),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text(text = "Lesson", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("quiz/$moduleId") {
                        popUpTo("moduleDetails/$moduleId") { inclusive = true }
                    }
                },
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onTertiary),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text(text = "Quiz", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("game_selection") {
                        popUpTo("moduleDetails/$moduleId") { inclusive = true }
                    }
                },
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onTertiary),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text(text = "Games", fontSize = 30.sp)
            }
        }
    }
}
