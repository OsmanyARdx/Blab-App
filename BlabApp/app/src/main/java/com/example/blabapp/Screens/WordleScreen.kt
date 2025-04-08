package com.example.blabapp.Screens


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blabapp.ViewModels.WordleViewModel



@Composable
fun WordleScreen(viewModel: WordleViewModel = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        viewModel.guesses.forEach { guess ->
            WordRow(guess, viewModel.getFeedback(guess))
        }

        TextField(
            value = viewModel.currentInput,
            onValueChange = { viewModel.onInputChange(it) },
            label = { Text("Enter a word") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.submitGuess() },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.currentInput.length == 5
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun WordRow(word: String, feedback: List<Color>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        word.forEachIndexed { index, char ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(feedback[index], shape = RoundedCornerShape(8.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = char.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
