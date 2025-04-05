package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.blabapp.Nav.QuizQuestion
import com.example.blabapp.ui.theme.BlabBlue
import com.example.blabapp.ui.theme.BlabGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.Normalizer

@Composable
fun QuizScreen(navController: NavHostController, moduleId: String) {

    val currentQuestionIndex = rememberSaveable { mutableStateOf(0) }
    val selectedAnswer = rememberSaveable { mutableStateOf<String?>(null) }
    val showResult = rememberSaveable { mutableStateOf(false) }
    val selectedAnswers = rememberSaveable { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val userInput = rememberSaveable { mutableStateOf("") }

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

        selectedAnswers.value = emptyMap()
        selectedAnswer.value = null
        showResult.value = false
        currentQuestionIndex.value = 0

        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val learningPreference = userDoc.getString("learning") ?: "ES"
            val quizCollection = if (learningPreference == "ES") "quizForLearningES" else "quizForLearningEN"

            val quizSnapshot = firestore.collection("modules").document(moduleId)
                .collection("quizes").get().await()

            val allQuestions = mutableListOf<QuizQuestion>()

            for (quizDoc in quizSnapshot.documents) {
                val subCollectionSnapshot = quizDoc.reference.collection(quizCollection).get().await()
                val items = subCollectionSnapshot.documents.mapNotNull { doc ->
                    val type = doc.getString("questionType") ?: "multipleChoice"
                    if (learningPreference == "EN") {
                        val question = doc.getString("pregunta")
                        val options = (doc.get("opciones") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val correctAnswer = doc.getString("respuesta")
                        if (question != null && correctAnswer != null) {
                            QuizQuestion(question, options, correctAnswer, type)
                        } else null
                    } else {
                        val question = doc.getString("question")
                        val options = (doc.get("options") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val correctAnswer = doc.getString("answer")
                        if (question != null && correctAnswer != null) {
                            QuizQuestion(question, options, correctAnswer, type)
                        } else null
                    }
                }
                allQuestions.addAll(items)
            }

            quizQuestions.value = allQuestions
        } catch (e: Exception) {
            Log.e("Quiz", "Error loading quiz: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading.value) {
            CircularProgressIndicator()
        } else if (quizQuestions.value.isNotEmpty()) {
            val index = currentQuestionIndex.value

            Crossfade(targetState = index) { questionIndex ->
                val currentQuestion = quizQuestions.value[questionIndex]

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Question ${questionIndex + 1}/${quizQuestions.value.size}",
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

                    if (currentQuestion.questionType == "fillInBlank") {
                        OutlinedTextField(
                            value = userInput.value,
                            onValueChange = { userInput.value = it },
                            label = { Text("Your Answer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val answer = userInput.value.trim()
                                selectedAnswer.value = answer
                                selectedAnswers.value = selectedAnswers.value + (index to answer)
                                showResult.value = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Submit")
                        }
                    } else {
                        currentQuestion.options.forEach { option ->
                            val isSelected = selectedAnswer.value == option
                            val isCorrect = option == currentQuestion.answer

                            when {
                                showResult.value && isCorrect -> BlabGreen
                                showResult.value && isSelected -> Color.Red
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Button(
                                onClick = {
                                    if (!showResult.value) {
                                        selectedAnswer.value = option
                                        selectedAnswers.value = selectedAnswers.value + (index to option)
                                        showResult.value = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                enabled = !showResult.value,
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (showResult.value) {
                        val isCorrect = normalize(selectedAnswer.value.orEmpty()) == normalize(currentQuestion.answer)

                        Text(
                            text = if (isCorrect) "Correct!" else "Wrong! The correct answer is ${currentQuestion.answer}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) BlabGreen else Color.Red
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                showResult.value = false
                                selectedAnswer.value = null
                                userInput.value = ""
                                if (index < quizQuestions.value.size - 1) {
                                    currentQuestionIndex.value++
                                } else {
                                    if (quizQuestions.value.isNotEmpty()) {
//                                        if (!isCorrect && userId != null) {
//                                            CoroutineScope(Dispatchers.IO).launch {
//                                                saveWrongAnswerToFirestore(userId, moduleId, currentQuestion, selectedAnswer.value.orEmpty())
//                                            }
//                                        }
                                        val score = calculateScore(quizQuestions.value, selectedAnswers.value)
                                        navController.navigate("quiz_score/$score/${quizQuestions.value.size}/${moduleId}")
                                    }
                                }

                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlabBlue)
                        ) {
                            Text(
                                text = if (index < quizQuestions.value.size - 1) "Next" else "Finish",
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
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
        val correctAnswer = questions.getOrNull(index)?.answer
        if (normalize(selectedAnswer) == normalize(correctAnswer ?: "")) {
            score++
        }
    }
    return score
}

fun normalize(text: String): String {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()
}

// fun saveWrongAnswerToFirestore(
//    userId: String,
//    moduleId: String,
//    question: QuizQuestion,
//    userAnswer: String
//) {
//    val firestore = FirebaseFirestore.getInstance()
//    val wrongAnswerData = mapOf(
//        "question" to question.question,
//        "correctAnswer" to question.correctAnswer,
//        "userAnswer" to userAnswer,
//        "timestamp" to System.currentTimeMillis()
//    )
//
//    firestore.collection("users")
//        .document(userId)
//        .collection("wrongAnswers")
//        .document(moduleId)
//        .collection("questions")
//        .add(wrongAnswerData)
//}