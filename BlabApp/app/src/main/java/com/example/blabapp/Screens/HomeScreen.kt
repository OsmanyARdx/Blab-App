package com.example.blabapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blabapp.ui.theme.BlabPurple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.ui.text.style.TextAlign
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

import android.content.Context
import android.content.SharedPreferences
import android.os.Message
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import com.example.blabapp.Screens.MessagesScreen
import com.example.blabapp.Screens.SidebarMenu
import com.example.blabapp.ui.theme.BlabYellow
import com.example.blabapp.ui.theme.Pink80
import com.example.blabapp.ui.theme.Purple40
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.Repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(title: String, navController: NavHostController, context: Context) {
    val userStreak = remember { mutableStateOf("Loading...") }
    val userRank = remember { mutableStateOf("Loading...") }
    val userName = remember { mutableStateOf("Loading...") }
    val profileImageUrl = remember { mutableStateOf("") }
    val (phraseInEnglish, phraseInSpanish) = rememberPhraseOfTheDay(context)
    val isSpanish = remember { mutableStateOf(true) }
    val isSidebarVisible = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Realtime listener for profile updates
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userName.value = snapshot.getString("name") ?: "User"
                        profileImageUrl.value = snapshot.getString("imageUrl") ?: ""
                        userStreak.value = snapshot.getLong("userStreak")?.toString() ?: "Loading..."
                        userRank.value = snapshot.getString("userRank") ?: "Loading..."
                    }
                }
        }
    }

    Column(Modifier.padding(0.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(7.dp)
        ) {
            IconButton(onClick = { isSidebarVisible.value = !isSidebarVisible.value }, modifier = Modifier.align(Alignment.TopStart)) {
                if (profileImageUrl.value.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl.value),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, BlabPurple, CircleShape)
                            .background(BlabPurple),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile_photo),
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, BlabPurple, CircleShape)
                            .background(BlabPurple)
                    )
                }
            }

            IconButton(onClick = { navController.navigate("messages_screen") }, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Messenger",
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }

            Text(
                text = userRank.value,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(bottom = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Streak: ${userStreak.value}",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (profileImageUrl.value.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl.value),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .border(1.dp, BlabPurple, CircleShape)
                            .background(BlabPurple),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile_photo),
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .border(1.dp, BlabPurple, CircleShape)
                            .background(BlabPurple)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userName.value,
                    fontSize = 34.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(width = 300.dp, height = 100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, if (isSpanish.value) Pink80 else BlabPurple, RoundedCornerShape(50.dp))
                        .background(
                            animateColorAsState(targetValue = if (isSpanish.value) BlabPurple else Pink80).value,
                            RoundedCornerShape(50.dp)
                        )
                        .padding(1.dp)
                ) {
                    Button(
                        onClick = { isSpanish.value = !isSpanish.value },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Phrase of the Day:\n\n" + if (isSpanish.value) phraseInSpanish.value else phraseInEnglish.value,
                                color = if (isSpanish.value) Pink80 else BlabPurple,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Sidebar
    if (isSidebarVisible.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black.copy(alpha = 0.5f), RoundedCornerShape(0.dp)) // semi-transparent background
                .clickable { isSidebarVisible.value = false } // close sidebar on click outside
        ) {
            SidebarMenu(navController) // Sidebar content
        }
    }
}



// Fetches an English phrase and translates it to Spanish
fun fetchAndTranslateRandomPhrase(onResult: (String, String) -> Unit) {
    val url = "https://tatoeba.org/eng/api_v0/search?from=eng&limit=50"

    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult("Error", "Failed to fetch phrase")
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { json ->
                try {
                    val jsonObject = JSONObject(json)
                    val resultsArray = jsonObject.getJSONArray("results")
                    val englishSentences = mutableListOf<String>()

                    for (i in 0 until resultsArray.length()) {
                        val result = resultsArray.getJSONObject(i)
                        val englishText = result.getString("text")
                        englishSentences.add(englishText)
                    }

                    if (englishSentences.isNotEmpty()) {
                        val randomSentence = englishSentences.random()
                        translateSentence(randomSentence) { translatedText ->
                            onResult(randomSentence, translatedText)
                        }
                    } else {
                        onResult("Error", "No sentences found")
                    }
                } catch (e: Exception) {
                    onResult("Error", "Parsing error: ${e.message}")
                }
            } ?: onResult("Error", "No response from server")
        }
    })
}

// Translates an English sentence to Spanish
fun translateSentence(sentence: String, onResult: (String) -> Unit) {
    val encodedSentence = URLEncoder.encode(sentence, "UTF-8")
    val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=es&dt=t&q=$encodedSentence"

    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult("Failed to translate sentence")
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { json ->
                try {
                    val jsonArray = JSONArray(json)
                    val translatedText = jsonArray.getJSONArray(0).getJSONArray(0).getString(0)
                    onResult(translatedText)
                } catch (e: Exception) {
                    onResult("Error parsing translation")
                }
            } ?: onResult("No response from server")
        }
    })
}



@Composable
fun rememberPhraseOfTheDay(context: Context): Pair<MutableState<String>, MutableState<String>> {
    val sharedPreferences = context.getSharedPreferences("PhrasePrefs", Context.MODE_PRIVATE)
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val storedDate = sharedPreferences.getString("lastFetchDate", null)
    val storedEnglish = sharedPreferences.getString("phraseEnglish", "Fetching...")
    val storedSpanish = sharedPreferences.getString("phraseSpanish", "Fetching...")

    val phraseInEnglish = remember { mutableStateOf(storedEnglish ?: "Fetching...") }
    val phraseInSpanish = remember { mutableStateOf(storedSpanish ?: "Fetching...") }

    LaunchedEffect(Unit) {
        if (storedDate != todayDate) {
            fetchAndTranslateRandomPhrase { english, spanish ->
                phraseInEnglish.value = english
                phraseInSpanish.value = spanish

                sharedPreferences.edit().apply {
                    putString("lastFetchDate", todayDate)
                    putString("phraseEnglish", english)
                    putString("phraseSpanish", spanish)
                    apply()
                }
            }
        }
    }

    return Pair(phraseInEnglish, phraseInSpanish)
}
