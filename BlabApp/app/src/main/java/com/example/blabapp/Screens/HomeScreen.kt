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
import java.text.SimpleDateFormat
import java.util.*

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

                // Save new phrase and date
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

@Composable
fun HomeScreen(title: String, navController: NavHostController, profileImageUrl: String, context: Context) {
    val userStreak = remember { mutableStateOf("Loading...") }
    val userRank = remember { mutableStateOf("Loading...") }
    val userName = remember { mutableStateOf("Loading...") }
    val (phraseInEnglish, phraseInSpanish) = rememberPhraseOfTheDay(context)
    val isSpanish = remember { mutableStateOf(true) }

    // Fetch user data
    LaunchedEffect(Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userStreak.value = document.getString("userStreak") ?: "0"
                        userRank.value = document.getString("userRank") ?: "Default"
                        userName.value = document.getString("name") ?: "User"
                    }
                }
                .addOnFailureListener {
                    userStreak.value = "Error"
                    userRank.value = "Error"
                }
        } else {
            userStreak.value = "Not Logged In"
            userRank.value = "Not Available"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Messenger",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable { navController.navigate("messages") }
        )

        Text(
            text = userRank.value,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            color = MaterialTheme.colorScheme.secondary
        )
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

            Image(
                painter = painterResource(id = R.drawable.pfp),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .border(1.dp, BlabPurple, CircleShape)
                    .background(BlabPurple)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userName.value,
                fontSize = 34.sp,
                color = BlabPurple,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(3.dp, BlabPurple, RoundedCornerShape(16.dp))
                    .padding(1.dp)
            ) {
                Button(
                    onClick = { isSpanish.value = !isSpanish.value },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column {
                        Text(
                            text = "Phrase of the Day:\n\n" +
                                    if (isSpanish.value) phraseInSpanish.value else phraseInEnglish.value,
                            color = BlabPurple,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


// Fetches an English phrase and translates it to Spanish
fun fetchAndTranslateRandomPhrase(onResult: (String, String) -> Unit) {
    val url = "https://tatoeba.org/eng/api_v0/search?from=eng&limit=200"

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
                        Log.d("sentences", englishSentences.toString())
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
