package com.example.blabapp.Screens

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blabapp.Nav.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.example.blabapp.Nav.Module
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.ui.theme.BlabGreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LessonScreen(navController: NavHostController, moduleId: String) {

    Log.d("Tap", "Loaded LessonScreen")
    val lessons = remember { mutableStateOf<List<Lesson>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val modules = getModule()

    val wordIndex = remember { mutableStateOf(0) }
    val lessonIndex = remember { mutableStateOf(0) }

    val displayLabels = listOf("Definition", "Sentence", "Translation")


    // 🟢 Initialize Text-to-Speech ONCE when the screen loads
    val context = LocalContext.current
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }


    fun speakText(text: String) {
        if (ttsReady) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w("TTS", "TTS not ready yet!")
        }
    }


    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    LaunchedEffect(moduleId) {
        val db = FirebaseFirestore.getInstance()
        val tempLessons = mutableListOf<Lesson>()



        Log.d("userid", userId.toString())
        db.collection("users").document(userId.toString())

            .get()
            .addOnSuccessListener { userDoc ->
                val learningPreference = userDoc.getString("learning") ?: "ES"
                val subCollection = if (learningPreference == "ES") "learningES" else "learningEN"

                db.collection("modules").document(moduleId)
                    .collection("learning")
                    .get()
                    .addOnSuccessListener { learningSnapshot ->
                        val learningDocs = learningSnapshot.documents
                        if (learningDocs.isEmpty()) {
                            isLoading.value = false
                            return@addOnSuccessListener
                        }

                        var completedRequests = 0
                        learningDocs.forEach { learningDoc ->
                            learningDoc.reference.collection(subCollection)
                                .get()
                                .addOnSuccessListener { subCollectionSnapshot ->
                                    tempLessons.addAll(
                                        subCollectionSnapshot.documents.mapNotNull { doc ->
                                            val isSpanish = learningPreference == "ES"
                                            Lesson(
                                                word = doc.getString(if (isSpanish) "word" else "palabra").toString(),
                                                definition = doc.getString(if (isSpanish) "definition" else "definición"),
                                                sentence = doc.getString(if (isSpanish) "sentence" else "oración"),
                                                translation = doc.getString(if (isSpanish) "translation" else "traducción")
                                            )
                                        }
                                    )

                                    completedRequests++
                                    if (completedRequests == learningDocs.size) {
                                        lessons.value = tempLessons
                                        isLoading.value = false
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Firestore", "Error fetching $subCollection: ${exception.message}")
                                    isLoading.value = false
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firestore", "Error fetching learning: ${exception.message}")
                        isLoading.value = false
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching user data: ${exception.message}")
            }
    }


    val userLearningPref = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("users").document(it)
                .get()
                .addOnSuccessListener { userDoc ->
                    userLearningPref.value = userDoc.getString("learning") ?: "ES" // Default to "ES"
                    Log.d("learning", userLearningPref.toString())
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Error fetching user learning preference: ${exception.message}")
                }
        }
    }

    DisposableEffect(userLearningPref.value) {
        if (userLearningPref.value != null) {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val locale = if (userLearningPref.value == "EN") Locale("en", "US") else Locale("es", "ES")
                    val result = textToSpeech?.setLanguage(locale)

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language $locale is not supported!")
                    } else {
                        ttsReady = true
                    }
                } else {
                    Log.e("TTS", "TTS Initialization failed!")
                }
            }
        }

        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }




    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigate("modules") },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading.value) {
                CircularProgressIndicator()
            } else if (lessons.value.isNotEmpty()) {
                val currentLesson = lessons.value[wordIndex.value]

                AnimatedContent(targetState = currentLesson) { targetLesson ->

                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(450.dp)
                            .padding(16.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = BlabGreen)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Word: ${targetLesson.word}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black
                                )


                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = "Showing: ${displayLabels[lessonIndex.value]}",
                                    fontSize = 18.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                val currentText = when (lessonIndex.value) {
                                    0 -> targetLesson.definition ?: "N/A"
                                    1 -> targetLesson.sentence ?: "N/A"
                                    2 -> targetLesson.translation ?: "N/A"
                                    else -> targetLesson.definition ?: "N/A"

                                }

                                Text(
                                    text = currentText,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(32.dp))



                                IconButton(
                                    onClick = { speakText(targetLesson.word) },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Icon(
                                        modifier = Modifier.size(50.dp),
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Play Audio",
                                        tint = Color.Black,
                                    )
                                }
                            }
                        }
                        Text(text = "Back", color = MaterialTheme.colorScheme.onTertiary)
                    }

                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        modifier = Modifier
                            .border(2.dp, Color.Black, RoundedCornerShape(50.dp))
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.tertiary),
                        onClick = {
                            if (lessonIndex.value > 0) {
                                lessonIndex.value--
                            } else {
                                if (wordIndex.value > 0) {
                                    wordIndex.value--
                                    lessonIndex.value = 2

                                } else {
                                    navController.navigate("modules") {
                                        popUpTo("modules") { inclusive = true }
                                    }
                                }
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = "Back")
                    }

                    Button(
                        modifier = Modifier
                            .border(2.dp, Color.Black, RoundedCornerShape(50.dp))
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.tertiary),
                        onClick = {
                            if (lessonIndex.value < 2) {
                                lessonIndex.value++
                            } else {
                                lessonIndex.value = 0
                                if (wordIndex.value < lessons.value.size - 1) {
                                    wordIndex.value++
                                } else {
                                    navController.navigate("modules") {
                                        popUpTo("modules") { inclusive = true }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = Color.Black
                        )
                    ) {
                        val nextText = if (wordIndex.value == lessons.value.size - 1 && lessonIndex.value == 2) "Complete" else "Next"
                        Text(text = nextText)
                    }
                }

            }
        }
    }
}

@Composable
fun ModuleName(module: Module) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Module #${module.moduleNum}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiary
        )
    }
}


