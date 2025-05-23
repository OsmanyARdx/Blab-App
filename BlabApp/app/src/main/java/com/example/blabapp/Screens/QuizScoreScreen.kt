package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.Repository.UserRepository.refreshUser
import com.example.blabapp.ui.theme.BlabGreen
import com.example.blabapp.ui.theme.BlabRed
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun QuizScoreScreen(navController: NavHostController, score: Int, totalQuestions: Int, moduleNum: String) {
    val scorePercentage = (score.toFloat() / totalQuestions.toFloat()) * 100
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Quiz Completed!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$score / $totalQuestions",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (score >= totalQuestions / 2) BlabGreen else BlabRed
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You scored ${"%.2f".format(scorePercentage)}%",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                modifier = Modifier.fillMaxWidth(.7f).align(Alignment.CenterHorizontally),
                onClick = {
                    // Update Firestore after quiz completion
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (scorePercentage.toInt() == 100 && userId != null) {
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(userId)
                        userRef.update("completeMod", FieldValue.arrayUnion(moduleNum))
                    }

                    upgradeUserRank(userId.toString())
                    // Refresh the user repository before navigating back to modules
                    coroutineScope.launch {
                        UserRepository.refreshUser()
                    }

                    navController.navigate("modules") {
                        // Prevent duplicate back stack entries
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Return to Module",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}


fun upgradeUserRank(userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userRef = firestore.collection("users").document(userId)

    userRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val currentRank = document.getString("userRank") ?: "Simple Student"
                val newRank = getNextRank(currentRank)

                if (newRank != currentRank) { // Upgrade only if different
                    userRef.update("userRank", newRank)
                        .addOnSuccessListener {

                            Log.d("Firebase", "User rank upgraded to $newRank")
                            val coroutineScope = CoroutineScope(Dispatchers.IO)
                            coroutineScope.launch {
                                refreshUser()
                            }

                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to update user rank", e)
                        }
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Failed to fetch user rank", e)
        }
}

fun getNextRank(currentRank: String): String {
    return when (currentRank) {
        "Simple Student" -> "Budding Beginner"
        "Budding Beginner" -> "Clever Communicator"
        "Clever Communicator" -> "Masterful Multilingual"
        "Masterful Multilingual" -> "Fluent Fellow"
        else -> currentRank
    }
}


