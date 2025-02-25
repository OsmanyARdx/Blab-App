package com.example.blabapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // UI to launch AR activity
            MainScreen()
        }
    }

    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Start AR Experience")
            }
        }
    }

    @Preview
    @Composable
    fun PreviewMainScreen() {
        MainScreen()
    }
}
