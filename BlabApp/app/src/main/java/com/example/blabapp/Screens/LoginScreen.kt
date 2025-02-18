package com.example.blabapp.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blabapp.Design.InputField
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.ViewModels.LoginScreenViewModel
import com.example.blabapp.ui.theme.Pink40
import com.example.blabapp.ui.theme.Pink80


/**
 * Displays AddSaleScreen which contains the text
 * fields and button to add a sale to the database
 */
@Composable
fun LoginScreen(accountRepository: AccountRepository, navController: NavController){
    val viewModel = viewModel { LoginScreenViewModel(accountRepository) }
    var rememberEmail by rememberSaveable { mutableStateOf("") }
    var rememberPassword by rememberSaveable { mutableStateOf("") }
    var success by rememberSaveable {
        mutableStateOf(true)
    }
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Pink80),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login", fontSize = 30.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            InputField("Email Address", email) { email = it }
            InputField("Password", password, isPassword = true) { password = it }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Logged in with: $email", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Pink40)
            ) {
                Text(text = "Login", fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(text = "Don't have an account?")
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Register",
                    color = Color.Blue,
                    modifier = Modifier.clickable { navController.navigate("RegisterScreen") }
                )
            }
        }
    }
}


@Composable
fun InputField(label: String, value: String, isPassword: Boolean = false, onValueChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Enter $label", color = Color.White) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Pink40, unfocusedContainerColor = Pink40,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(14.dp)
        )
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