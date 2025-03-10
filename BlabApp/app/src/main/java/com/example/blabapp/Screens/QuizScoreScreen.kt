package com.example.blabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import com.example.blabapp.ui.theme.BlabGreen

@Composable
fun QuizScoreScreen(navController: NavHostController, score: Int, totalQuestions: Int) {
    val scorePercentage = (score.toFloat() / totalQuestions.toFloat()) * 100

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Quiz Completed!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$score / $totalQuestions",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (score >= totalQuestions / 2) BlabGreen else Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You scored ${"%.2f".format(scorePercentage)}%",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("modules")},
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Return to Module",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}
