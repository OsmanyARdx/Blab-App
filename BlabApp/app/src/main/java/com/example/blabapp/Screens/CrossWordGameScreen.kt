package com.example.blabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.random.Random

data class ScrambleWord(
    val word: String,
    val startX: Int,
    val startY: Int,
    val direction: Direction
)

enum class Direction {
    HORIZONTAL,
    VERTICAL
}

@Composable
fun CrossWordGameScreen(
    gridSize: Int = 8,
    wordsToFind: List<String>,
    navController: NavController,
    onGameOver: (score: Int) -> Unit
) {
    val scrambleState = remember { generateScrambleGrid(gridSize, wordsToFind) }
    var selectedCoords by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var foundWords by remember { mutableStateOf(mutableSetOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Find the hidden words!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.height(16.dp))

        // Grid
        for (y in 0 until gridSize) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (x in 0 until gridSize) {
                    val letter = scrambleState.grid[y][x]
                    val isSelected = selectedCoords.contains(x to y)
                    val isPartOfFoundWord = scrambleState.wordPositions.any { wordPos ->
                        wordPos.word in foundWords &&
                                getWordCoordinates(wordPos).contains(x to y)
                    }

                    val bgColor = when {
                        isPartOfFoundWord -> Color(0xFFAAF683)
                        isSelected -> Color(0xFFD0E8FF)
                        else -> Color.White
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .background(bgColor)
                            .clickable {
                                selectedCoords = selectedCoords + (x to y)
                                val selectedWord = selectedCoords.map { (sx, sy) -> scrambleState.grid[sy][sx] }
                                    .joinToString("")
                                val reverseWord = selectedWord.reversed()

                                val match = scrambleState.wordPositions.firstOrNull {
                                    (it.word == selectedWord || it.word == reverseWord) &&
                                            getWordCoordinates(it) == selectedCoords
                                }

                                if (match != null) {
                                    foundWords.add(match.word)
                                    selectedCoords = listOf()
                                } else if (selectedWord.length > scrambleState.longestWordLength) {
                                    selectedCoords = listOf()
                                }
                            }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            letter.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Found: ${foundWords.size} / ${wordsToFind.size}",
            fontSize = 16.sp,
            color = Color.Black
        )

        if (foundWords.size == wordsToFind.size) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                onGameOver(foundWords.size)
                navController.navigate("games")
            }) {
                Text("Finish Game", color = Color.Black)
            }
        }
    }
}

data class ScrambleGrid(
    val grid: Array<CharArray>,
    val wordPositions: List<ScrambleWord>,
    val longestWordLength: Int
)

fun generateScrambleGrid(size: Int, words: List<String>): ScrambleGrid {
    val grid = Array(size) { CharArray(size) { '-' } }
    val wordPositions = mutableListOf<ScrambleWord>()
    var longestWord = 0

    for (word in words) {
        val upperWord = word.uppercase()
        var placed = false
        var attempts = 0

        while (!placed && attempts < 100) {
            val direction = if (Random.nextBoolean()) Direction.HORIZONTAL else Direction.VERTICAL
            val maxX = if (direction == Direction.HORIZONTAL) size - upperWord.length else size - 1
            val maxY = if (direction == Direction.VERTICAL) size - upperWord.length else size - 1

            val startX = Random.nextInt(0, maxX + 1)
            val startY = Random.nextInt(0, maxY + 1)

            placed = placeWord(grid, upperWord, startX, startY, direction)
            if (placed) {
                wordPositions.add(ScrambleWord(upperWord, startX, startY, direction))
                if (upperWord.length > longestWord) longestWord = upperWord.length
            }

            attempts++
        }
    }

    // Fill in remaining '-' with random letters
    for (y in 0 until size) {
        for (x in 0 until size) {
            if (grid[y][x] == '-') {
                grid[y][x] = getRandomChar()
            }
        }
    }

    return ScrambleGrid(grid, wordPositions, longestWord)
}

fun placeWord(
    grid: Array<CharArray>,
    word: String,
    startX: Int,
    startY: Int,
    direction: Direction
): Boolean {
    for (i in word.indices) {
        val x = if (direction == Direction.HORIZONTAL) startX + i else startX
        val y = if (direction == Direction.VERTICAL) startY + i else startY

        if (grid[y][x] != '-' && grid[y][x] != word[i]) {
            return false
        }
    }

    for (i in word.indices) {
        val x = if (direction == Direction.HORIZONTAL) startX + i else startX
        val y = if (direction == Direction.VERTICAL) startY + i else startY
        grid[y][x] = word[i]
    }

    return true
}

fun getWordCoordinates(word: ScrambleWord): List<Pair<Int, Int>> {
    return word.word.indices.map {
        val x = if (word.direction == Direction.HORIZONTAL) word.startX + it else word.startX
        val y = if (word.direction == Direction.VERTICAL) word.startY + it else word.startY
        x to y
    }
}

fun getRandomChar(): Char = ('A'..'Z').random()

@Composable
fun ScrambleScreen(navController: NavController) {
    val wordList = listOf("Perro", "Casa", "Sol", "Libro")

    CrossWordGameScreen(wordsToFind = wordList, navController = navController) { score ->
        println("Game finished! Found $score words.")
    }
}
