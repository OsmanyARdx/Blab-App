package com.example.blabapp.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.NavHostController
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import com.example.blabapp.R
import com.example.blabapp.ui.theme.BlabBlue

@Composable
fun GameSelectionScreen(navController: NavHostController) {
    // Defining the background color with 25% opacity for the semi-transparent look
    val semiTransparentBg = Color(0x40000000) // 25% opacity (75% transparency)

    // State to track which game box was clicked
    val clickedIndex = remember { mutableStateOf(-1) }

    // Set up zoom animation
    val scale = animateFloatAsState(
        targetValue = if (clickedIndex.value >= 0) 1.5f else 1f,
        animationSpec = tween(durationMillis = 500)
    )

    // A list of different images for each game (replace with your own images)
    val gameImages = listOf(
        R.drawable.crossword,
        R.drawable.matchcards,
        R.drawable.listen,
        R.drawable.crossword
    )

    // Layout for the 2x2 grid of games
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns for 2x2 grid
        modifier = Modifier.fillMaxSize(), // Fill the whole screen
        verticalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
    ) {
        items(3) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize() // Make each item fill available space
                    .background(semiTransparentBg, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Load the image for the current game
                val gameImage = painterResource(id = gameImages[index])

                // Image inside the box with zoom effect and 80% opacity
                Image(
                    painter = gameImage,
                    contentDescription = "Game Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.2f)  // Apply 80% opacity here
                        .scale(scale.value), // Animate zoom effect
                    contentScale = ContentScale.Crop
                )

                Button(
                    onClick = {
                        // Navigate to the corresponding game screen based on the index
                        when (index) {
                            0 -> navController.navigate("game1")
                            1 -> navController.navigate("game2")
                            2 -> navController.navigate("game3")
                            3 -> navController.navigate("game4")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlabBlue)
                ) {
                    Text(
                        text = "Game ${index + 1}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
