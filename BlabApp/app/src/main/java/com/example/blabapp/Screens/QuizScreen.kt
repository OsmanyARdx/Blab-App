package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.blabapp.Nav.QuizQuestion
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.ui.theme.BlabBlue
import com.example.blabapp.ui.theme.BlabGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(navController: NavHostController, moduleId: String) {
    val currentQuestionIndex = remember { mutableStateOf(0) }
    val selectedAnswer = remember { mutableStateOf<String?>(null) }
    val showResult = remember { mutableStateOf(false) }
    val selectedAnswers = remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val userLearning = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()


    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    val quizQuestions = remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(moduleId) {
        if (userId == null) {
            isLoading.value = false
            return@LaunchedEffect
        }

        // Step 1: Fetch user learning preference
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val learningPreference = userDoc.getString("learning") ?: "ES" // Default to "ES"
                val quizCollection =
                    if (learningPreference == "ES") "quizForLearningES" else "quizForLearningEN"

                // Step 2: Fetch quizzes under the module
                firestore.collection("modules").document(moduleId)
                    .collection("quizes")
                    .get()
                    .addOnSuccessListener { quizSnapshot ->
                        val quizDocs = quizSnapshot.documents
                        if (quizDocs.isEmpty()) {
                            isLoading.value = false
                            return@addOnSuccessListener
                        }

                        val allQuestions = mutableListOf<QuizQuestion>()
                        var completedRequests = 0

                        // Step 3: Fetch questions from each quiz subcollection
                        quizDocs.forEach { quizDoc ->
                            quizDoc.reference.collection(quizCollection)
                                .get()
                                .addOnSuccessListener { subCollectionSnapshot ->
                                    val items = subCollectionSnapshot.documents.mapNotNull { doc ->
                                        if (learningPreference == "EN") {
                                            val question = doc.getString("pregunta")
                                            val optionsList = doc.get("opciones") as? List<*>
                                            val options = optionsList?.filterIsInstance<String>()
                                                ?: emptyList()
                                            val correctAnswer = doc.getString("respuesta")

                                            if (question != null && options.isNotEmpty() && correctAnswer != null) {
                                                QuizQuestion(question, options, correctAnswer)
                                            } else null
                                        } else {
                                            val question = doc.getString("question")
                                            val optionsList = doc.get("options") as? List<*>
                                            val options = optionsList?.filterIsInstance<String>()
                                                ?: emptyList()
                                            val correctAnswer = doc.getString("answer")

                                            if (question != null && options.isNotEmpty() && correctAnswer != null) {
                                                QuizQuestion(question, options, correctAnswer)
                                            } else null
                                        }
                                    }

                                    allQuestions.addAll(items)
                                    completedRequests++

                                    // If all quizzes are processed, update UI
                                    if (completedRequests == quizDocs.size) {
                                        quizQuestions.value = allQuestions
                                        isLoading.value = false
                                    }
                                }
                                .addOnFailureListener {
                                    completedRequests++
                                    if (completedRequests == quizDocs.size) {
                                        isLoading.value = false
                                    }
                                }
                        }
                    }
                    .addOnFailureListener {
                        isLoading.value = false
                    }
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }


    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading.value) {
            CircularProgressIndicator()
        } else if (quizQuestions.value.isNotEmpty()) {
            val currentQuestion = quizQuestions.value[currentQuestionIndex.value]

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Question ${currentQuestionIndex.value + 1}/${quizQuestions.value.size}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentQuestion.question,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiary
                )

                Spacer(modifier = Modifier.height(24.dp))

                currentQuestion.options.forEach { option ->
                    val isSelected = selectedAnswer.value == option
                    val isCorrect = option == currentQuestion.correctAnswer

                    Button(
                        onClick = {
                            selectedAnswer.value = option
                            selectedAnswers.value = selectedAnswers.value + (currentQuestionIndex.value to option)
                            showResult.value = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                showResult.value && isSelected -> if (isCorrect) BlabGreen else Color.Red
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text(text = option, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (showResult.value) {
                    val isCorrect = selectedAnswer.value == currentQuestion.correctAnswer
                    Text(
                        text = if (isCorrect) "Correct!" else "Wrong! The correct answer is ${currentQuestion.correctAnswer}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) BlabGreen else Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showResult.value = false
                            selectedAnswer.value = null
                            if (currentQuestionIndex.value < quizQuestions.value.size - 1) {
                                currentQuestionIndex.value++
                            } else {
                                val score = calculateScore(quizQuestions.value, selectedAnswers.value)
                                navController.navigate("quiz_score/$score/${quizQuestions.value.size}/${moduleId}")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlabBlue)
                    ) {
                        Text(text = if (currentQuestionIndex.value < quizQuestions.value.size - 1) "Next" else "Finish", color = MaterialTheme.colorScheme.onTertiary)
                    }
                }
            }
        } else {
            Column {
                Text(
                    text = "No quiz available for this module",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.onTertiary),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(text = "Back", fontSize = 20.sp)
                }
            }
        }
    }
}


fun calculateScore(questions: List<QuizQuestion>, selectedAnswers: Map<Int, String>): Int {
    var score = 0
    for ((index, selectedAnswer) in selectedAnswers) {
        val correctAnswer = questions.getOrNull(index)?.correctAnswer
        if (selectedAnswer == correctAnswer) {
            score++
        }
    }
    return score
}
