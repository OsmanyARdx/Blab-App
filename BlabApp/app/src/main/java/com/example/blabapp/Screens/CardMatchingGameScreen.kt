package com.example.blabapp.Screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blabapp.ui.theme.BlabBlue
import com.example.blabapp.ui.theme.BlabGreen

@Composable
fun CardMatchingGameScreen(navController: NavHostController) {
    val words = listOf(
        "hola" to "hello",
        "adios" to "goodbye",
        "gracias" to "thank you",
        "perro" to "dog",
        "gato" to "cat",
        "amigo" to "friend",
        "sol" to "sun",
        "libro" to "book"
    )

    // Randomly select 6 words for the game
    val selectedWords = words.shuffled().take(6)

    // Create shuffled cards and prepare state variables
    val shuffledCards = remember { (selectedWords + selectedWords.map { it.second to it.first }).shuffled() }
    val matchedPairs = remember { mutableStateOf(mutableSetOf<Int>()) }
    val flippedCards = remember { mutableStateOf<List<Int>>(emptyList()) }
    val allMatched = remember { mutableStateOf(false) } // Flag for completion
    val showDialog = remember { mutableStateOf(false) } // To control dialog visibility

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Card Matching Game",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lazy grid to display the cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // You can adjust this if needed
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(shuffledCards.size) { index ->
                if (!matchedPairs.value.contains(index)) {
                    val (word, _) = shuffledCards[index]
                    val isFlipped = flippedCards.value.contains(index)

                    // Set different colors for Spanish and English words
                    val cardColor = if (isFlipped) {
                        if (selectedWords.any { it.first == word }) Color.Cyan else Color.Magenta
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .aspectRatio(1.5f) // Change aspect ratio to make the card longer horizontally
                            .fillMaxHeight(0.25f), // Optional: Adjust the card height to fit the screen better
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        onClick = {
                            if (flippedCards.value.size < 2 && !isFlipped) {
                                flippedCards.value = flippedCards.value + index
                                if (flippedCards.value.size == 2) {
                                    // Call checkMatch to compare cards
                                    checkMatch(flippedCards, shuffledCards, matchedPairs, allMatched, showDialog)
                                }
                            }
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isFlipped) {
                                Text(
                                    text = word,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // Add invisible spacer to maintain grid layout after card disappears
                    Spacer(
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(1.5f) // Adjust to the same aspect ratio
                            .fillMaxWidth()
                    )
                }
            }
        }

        // Show the dialog when all cards are matched
        if (showDialog.value) {
            GoodJobDialog(onDismiss = {
                navController.navigate("modules") // Navigate to the next screen after dismiss
            })
        }
    }
}

// Function to check if flipped cards are a match
private fun checkMatch(
    flippedCards: MutableState<List<Int>>,
    shuffledCards: List<Pair<String, String>>,
    matchedPairs: MutableState<MutableSet<Int>>,
    allMatched: MutableState<Boolean>,
    showDialog: MutableState<Boolean> // Pass the showDialog state here
) {
    if (flippedCards.value.size == 2) {
        val (firstIndex, secondIndex) = flippedCards.value
        val firstPair = shuffledCards[firstIndex]
        val secondPair = shuffledCards[secondIndex]

        if ((firstPair.first == secondPair.second) || (firstPair.second == secondPair.first)) {
            Handler(Looper.getMainLooper()).postDelayed({
                matchedPairs.value.add(firstIndex)
                matchedPairs.value.add(secondIndex)
                flippedCards.value = emptyList()

                // Check if all pairs are matched AFTER updating matchedPairs
                if (matchedPairs.value.size == shuffledCards.size) {
                    allMatched.value = true // Set allMatched to true here
                    showDialog.value = true // Show the dialog
                }
            }, 1000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                flippedCards.value = emptyList()
            }, 1000)
        }
    }
}

// Dialog Composable to display the "Good Job!" message
@Composable
fun GoodJobDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Good Job!") },
        text = { Text("You have matched all the cards!") },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("OK")
            }
        }
    )
}

