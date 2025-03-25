package com.example.blabapp.ViewModels

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class WordleViewModel : ViewModel() {

    private val wordList = listOf("COMER", "JUGAR", "ADIOS", "VERDE", "LIMON", "CARRO", "FALDA", "NOVIO", "NOVIA")
    val secretWord = wordList[Random.nextInt(wordList.size)]

    var guesses by mutableStateOf(mutableListOf<String>())
        private set
    var currentInput by mutableStateOf("")
        private set

    fun onInputChange(newInput: String) {
        if (newInput.length <= 5) {
            currentInput = newInput.uppercase()
        }
    }

    fun submitGuess() {
        if (currentInput.length == 5 && currentInput !in guesses) {
            guesses.add(currentInput)
            currentInput = ""
        }
    }

    fun getFeedback(guess: String): List<Color> {
        return guess.mapIndexed { index, char ->
            when {
                char == secretWord[index] -> Color.Green  // Correct position
                char in secretWord -> Color.Yellow       // Wrong position
                else -> Color.Gray                       // Not in word
            }
        }
    }
}
