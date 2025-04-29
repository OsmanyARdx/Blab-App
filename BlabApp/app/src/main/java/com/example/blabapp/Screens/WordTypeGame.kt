package com.example.blabapp.Screens

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.R
import com.example.blabapp.ui.theme.BlabGreen
import com.example.blabapp.ui.theme.BlabLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun WordTypeGame(navController: NavHostController) {
    val context = LocalContext.current
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    val words = listOf(
        "pintar", "madre", "padre", "hermano", "adios",
        "hola", "buenos días", "correr", "comer"
    )
    val shuffledWords = remember { words.shuffled() }

    var currentWordIndex by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }
    var correctAnswers by remember { mutableStateOf(0) }  // Track correct answers
    var incorrectAnswers by remember { mutableStateOf(0) }  // Track incorrect answers
    var triesLeft by remember { mutableStateOf(3) }  // Track remaining tries for each question
    var timeLeft by remember { mutableStateOf(20) }
    var timerRunning by remember { mutableStateOf(true) }
    var gameComplete by remember { mutableStateOf(false) }  // State to track game completion

    // Word-to-image mapping
    val wordToImage = mapOf(
        "pintar" to R.drawable.paint,
        "madre" to R.drawable.madre,
        "padre" to R.drawable.padre,
        "hermano" to R.drawable.hermano,
        "adios" to R.drawable.goodbye,
        "hola" to R.drawable.hello,
        "buenos días" to R.drawable.goodmorning,
        "correr" to R.drawable.run,
        "comer" to R.drawable.eat,
    )

    // Initialize TTS
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // Set the language to Spanish
                    val result = textToSpeech?.setLanguage(Locale("es", "ES"))
                    ttsReady =
                        result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    fun speak(text: String) {
        if (ttsReady) {
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 2.0f) // 1.0 = full volume
            }
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
        }
    }

    fun nextWord(skipped: Boolean = false) {
        if (skipped) {
            incorrectAnswers++  // Increment incorrect answer count if the user skips or fails
            resultMessage = "Time’s up!"
        }
        if (currentWordIndex < shuffledWords.lastIndex) {
            currentWordIndex++
            userInput = ""
            triesLeft = 3  // Reset tries for the next word
            timeLeft = 20
            timerRunning = true
        } else {
            resultMessage = "Game Over! Correct: $correctAnswers, Incorrect: $incorrectAnswers."
            timerRunning = false
            gameComplete = true  // Set gameComplete to true when the game ends
        }
    }

    fun checkAnswer() {
        if (userInput.trim().equals(shuffledWords[currentWordIndex], ignoreCase = true)) {
            correctAnswers++  // Increment correct answer count
            resultMessage = "Correct!"
            triesLeft = 3  // Reset tries for the next word
            timerRunning = false
            nextWord()
        } else {
            triesLeft--
            if (triesLeft <= 0) {
                incorrectAnswers++  // Increment incorrect answer count if tries run out
                resultMessage = "Out of tries! Moving to next question."
                nextWord(skipped = true)
            } else {
                resultMessage = "Try again! Tries left: $triesLeft"
            }
        }
    }

    // Timer countdown effect
    LaunchedEffect(key1 = currentWordIndex, key2 = timerRunning) {
        if (timerRunning) {
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                timeLeft--
            }
            if (timeLeft == 0 && timerRunning) {
                nextWord(skipped = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = BlabLight,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(7.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }

                    Text(
                        text = "Word Type Game",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.align(Alignment.Center).padding(top = 16.dp)
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (gameComplete) {
                        // Center "Game Complete!!" message in the middle of the screen
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Game Complete!!",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlabGreen
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Correct Answers: $correctAnswers",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Incorrect Answers: $incorrectAnswers",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Normal game interface
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)

                            .fillMaxSize()
                                .padding(top = 70.dp, start = 24.dp, end = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                "Listen and Type the Word",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Correct: $correctAnswers | Incorrect: $incorrectAnswers",
                                fontSize = 18.sp,
                                color = BlabGreen,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                "Word ${currentWordIndex + 1} of ${shuffledWords.size}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary,
                            )

                            Text("Time Left: $timeLeft s", fontSize = 16.sp, color = Color.Red)

                            Spacer(modifier = Modifier.height(12.dp))  // Reduced space after the timer

                            // Display corresponding image for the word
                            val imageRes = wordToImage[shuffledWords[currentWordIndex]]
                            imageRes?.let {
                                Image(
                                    painter = painterResource(id = it),
                                    contentDescription = "Image for ${shuffledWords[currentWordIndex]}",
                                    modifier = Modifier.size(150.dp)
                                )
                            }

                            IconButton(
                                onClick = { speak(shuffledWords[currentWordIndex]) },
                                modifier = Modifier.size(100.dp) // sets the clickable area size
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Play Word",
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(50.dp) // sets the actual icon size
                                )
                            }

                            OutlinedTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                label = { Text("Your Answer") },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { checkAnswer() },
                                enabled = timeLeft > 0,
                                colors = ButtonDefaults.buttonColors( containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text("Submit")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = resultMessage,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiary
                            )

                        }
                    }
                }
            }
        }
    }
}

