package com.example.blabapp

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.reflect.KProperty



import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

import androidx.compose.ui.text.style.TextAlign

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Displays AddSaleScreen which contains the text
 * fields and button to add a sale to the database
 */
@Composable
fun LoginScreen(accountRepository: AccountRepository, navController: NavController){
    val viewModel = viewModel {LoginScreenViewModel(accountRepository)}
    var rememberEmail by rememberSaveable { mutableStateOf("") }
    var rememberPassword by rememberSaveable { mutableStateOf("") }
    var success by rememberSaveable {
        mutableStateOf(true)
    }

    Surface (
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        shadowElevation= 30.dp,
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Login", fontSize = 24.sp, color = Color.Blue)
            TextField(
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email") },
                value = rememberEmail,
                onValueChange = { rememberEmail = it })
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("Password") },
                value = rememberPassword,
                onValueChange = { rememberPassword = it})

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        println("true")
                        viewModel.loginFirebase(
                            rememberEmail,
                            rememberPassword,
                            {
                                success = true
                                navController.navigate("SalesListScreen")
                            },
                            {
                                success = false
                            }
                        )
                        rememberEmail = ""
                        rememberPassword = ""

                    }

                }
            )

            {
                Text(modifier = Modifier.fillMaxWidth(),text = "Login", textAlign = TextAlign.Center)
            }
            if (!success){
                failedLoginToast()
            }
        }
    }
}


/**
 * Closes on screen keyboard
 * Displays a toast letting the
 * user know the login failed
 */
@Composable
fun failedLoginToast(){
    // Hide the keyboard
    val inputMethodManager = LocalContext.current.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(LocalView.current.windowToken, 0)
    Toast.makeText(LocalContext.current, "Login unsuccessful", Toast.LENGTH_SHORT).show()
}

/*
private fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
    if (email.isBlank() || password.isBlank()) {
        onResult(false, "Email or password cannot be empty")
        return
    }



    firebaseAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("Authentication", "User authenticated successfully")
                onResult(true, "Login successful")
            } else {
                val error = task.exception?.message ?: "Unknown error occurred"
                Log.e("Authentication", "Error: $error")
                onResult(false, error)
            }
        }


}

private fun registerUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
    if (email.isBlank() || password.isBlank()) {
        onResult(false, "Email or password cannot be empty")
        return
    }


    firebaseAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("Registration", "User registered successfully")
                onResult(true, "Registration successful")
            } else {
                val error = task.exception?.message ?: "Unknown error occurred"
                Log.e("Registration", "Error: $error")
                onResult(false, error)
            }
        }

}

 */