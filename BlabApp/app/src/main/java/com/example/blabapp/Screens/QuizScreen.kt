package com.example.blabapp.Screens

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blabapp.ViewModels.QuizScreenViewModel
import com.example.blabapp.ViewModels.calculateScore
import com.example.blabapp.ViewModels.normalize
import com.example.blabapp.ui.theme.BlabBlue
import com.example.blabapp.ui.theme.BlabGreen
import com.google.firebase.auth.FirebaseAuth
import com.example.blabapp.ViewModels.saveWrongAnswerToFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    navController: NavHostController,
    moduleId: String,
    quizViewModel: QuizScreenViewModel = viewModel()
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val isLoading = quizViewModel.isLoading.value
    val quizQuestions = quizViewModel.quizQuestions.value

    val currentQuestionIndex = rememberSaveable { mutableStateOf(0) }
    val selectedAnswer = rememberSaveable { mutableStateOf<String?>(null) }
    val selectedAnswers = rememberSaveable { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val showResult = rememberSaveable { mutableStateOf(false) }
    val userInput = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(moduleId) {
        userId?.let {
            quizViewModel.loadQuizQuestions(it, moduleId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            quizQuestions.isNotEmpty() -> {
                Crossfade(targetState = currentQuestionIndex.value) { questionIndex ->
                    val currentQuestion = quizQuestions[questionIndex]

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Question ${questionIndex + 1}/${quizQuestions.size}",
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

                        // Answer Section
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
                                    selectedAnswers.value =
                                        selectedAnswers.value + (questionIndex to answer)
                                    showResult.value = true

                                    val isCorrect =
                                        normalize(answer) == normalize(currentQuestion.answer)
                                    if (!isCorrect && userId != null) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            saveWrongAnswerToFirestore(currentQuestion)
                                        }
                                    }
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

                                Button(
                                    onClick = {
                                        if (!showResult.value) {
                                            selectedAnswer.value = option
                                            selectedAnswers.value =
                                                selectedAnswers.value + (questionIndex to option)
                                            showResult.value = true

                                            if (!isCorrect && userId != null) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    saveWrongAnswerToFirestore(currentQuestion)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    enabled = !showResult.value,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            showResult.value && isCorrect -> BlabGreen
                                            showResult.value && isSelected -> Color.Red
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
                            val isCorrect = normalize(selectedAnswer.value.orEmpty()) == normalize(
                                currentQuestion.answer
                            )

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
                                    if (questionIndex < quizQuestions.size - 1) {
                                        currentQuestionIndex.value++
                                    } else {
                                        val score =
                                            calculateScore(quizQuestions, selectedAnswers.value)
                                        navController.navigate("quiz_score/$score/${quizQuestions.size}/$moduleId")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlabBlue)
                            ) {
                                Text(
                                    text = if (questionIndex < quizQuestions.size - 1) "Next" else "Finish",
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "No quiz available for this module",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
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
}