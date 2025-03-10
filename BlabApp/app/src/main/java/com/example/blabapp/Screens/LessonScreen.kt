package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blabapp.Nav.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun LessonScreen(navController: NavHostController, moduleId: String) {

    Log.d("Tap", "Loaded LessonScreen")
    val lessons = remember { mutableStateOf<List<Lesson>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    val wordIndex = remember { mutableStateOf(0) }
    val lessonIndex = remember { mutableStateOf(0) }

    val displayLabels = listOf("Definition", "Sentence", "Translation")

    LaunchedEffect(moduleId) {
        val db = FirebaseFirestore.getInstance()

        db.collection("modules")
            .document(moduleId)
            .collection("learning")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { doc ->
                    val word = doc.getString("word")
                    val definition = doc.getString("definition")
                    val sentence = doc.getString("sentence")
                    val translation = doc.getString("translation")

                    if (word != null) Lesson(word, definition, sentence, translation) else null
                }
                lessons.value = items
                isLoading.value = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching lessons: ${exception.message}")
                isLoading.value = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading.value) {
                CircularProgressIndicator()
            } else if (lessons.value.isNotEmpty()) {
                val currentLesson = lessons.value[wordIndex.value]

                Text(
                    text = "Word: ${currentLesson.word}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Showing: ${displayLabels[lessonIndex.value]}",
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onTertiary
                )

                Spacer(modifier = Modifier.height(32.dp))

                val currentText = when (lessonIndex.value) {
                    0 -> currentLesson.definition ?: "N/A"
                    1 -> currentLesson.sentence ?: "N/A"
                    2 -> currentLesson.translation ?: "N/A"
                    else -> currentLesson.definition ?: "N/A"
                }

                Text(
                    text = currentText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            if (lessonIndex.value > 0) {
                                lessonIndex.value-- // Move to the previous detail
                            } else {
                                if (wordIndex.value > 0) {
                                    wordIndex.value-- // Move to the previous word
                                    lessonIndex.value =
                                        2 // Start at the last detail of the previous word
                                } else {
                                    navController.navigate("modules") {
                                        popUpTo("modules") { inclusive = true }
                                    }
                                }
                            }
                        }
                    ) {
                        Text(text = "Back", color = MaterialTheme.colorScheme.onTertiary)
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            if (lessonIndex.value < 2) {
                                lessonIndex.value++ // Move to next detail
                            } else {
                                lessonIndex.value = 0 // Reset detail index
                                if (wordIndex.value < lessons.value.size - 1) {
                                    wordIndex.value++ // Move to next word
                                } else {
                                    navController.navigate("modules") {
                                        popUpTo("modules") { inclusive = true }
                                    }
                                }
                            }
                        }
                    ) {
                        Text(text = "Next", color = MaterialTheme.colorScheme.onTertiary)
                    }
                }

            }
        }
    }
}