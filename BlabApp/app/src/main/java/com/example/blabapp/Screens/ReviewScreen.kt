package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blabapp.Nav.WrongAnswer
import com.example.blabapp.ui.theme.BlabGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReviewScreen(navController: NavHostController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    val isLoading = remember { mutableStateOf(true) }
    val mistakes = remember { mutableStateOf<List<WrongAnswer>>(emptyList()) }
    val currentIndex = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (userId == null) return@LaunchedEffect

        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("errorTrack")
                .get()
                .await()

            mistakes.value = snapshot.documents.mapNotNull { doc ->
                val data = doc.toObject(WrongAnswer::class.java)
                data?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ReviewMistakes", "Error: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading.value -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            mistakes.value.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nothing to review.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { navController.navigate("modules") },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Back to Modules", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            else -> {
                val currentItem = mistakes.value.getOrNull(currentIndex.value)

                currentItem?.let { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .width(300.dp)
                                .height(450.dp)
                                .border(2.dp, Color.Black, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                        text = "Q: ${item.question}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Correct answer: ${item.answer}",
                                        fontSize = 24.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(32.dp))

                                    Button(
                                        onClick = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                userId?.let { clearUserError(it, item.id) }
                                                withContext(Dispatchers.Main) {
                                                    mistakes.value = mistakes.value.filter { it.id != item.id }
                                                    currentIndex.value = currentIndex.value.coerceAtMost(
                                                        mistakes.value.lastIndex
                                                    )
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                    ) {
                                        Text("Mark as Reviewed")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun clearUserError(userId: String, docId: String) {
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("errorTrack")
        .document(docId)
        .delete()
}
