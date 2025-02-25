package com.example.blabapp

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.blabapp.Nav.BlabApp
import com.example.blabapp.Screens.RootScreen
import com.example.blabapp.ui.theme.BlabAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var imm: InputMethodManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            BlabAppTheme {
                RootScreen()
            }
        }
    }
}