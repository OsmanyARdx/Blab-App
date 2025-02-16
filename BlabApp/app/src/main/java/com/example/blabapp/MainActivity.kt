package com.example.blabapp

import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blabapp.ui.theme.BlabAppTheme
import com.example.blabapp.ui.theme.Pink40
import com.example.blabapp.ui.theme.Pink80

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlabAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email or password cannot be empty")
            return
        }


        //to work when firebase get added
        /*
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
           */

    }

    private fun registerUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email or password cannot be empty")
            return
        }

        //to work when firebase get added
        /*
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
        */
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BlabAppTheme {
        Greeting("Android")
    }
}


@Composable
private fun RegistrationScreen(modifier: Modifier) {
    val context = LocalContext.current
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Pink80),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register", fontSize = 30.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            InputField("First Name", firstName) { firstName = it }
            InputField("Last Name", lastName) { lastName = it }
            InputField("Email Address", email) { email = it }
            InputField("Phone Number", phone) { phone = it }
            InputField("Password", password, isPassword = true) { password = it }
            InputField("Confirm Password", confirmPassword, isPassword = true) { confirmPassword = it }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Registered: $firstName $lastName", Toast.LENGTH_SHORT)
                        .show()
                },
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Pink40
                )

            ) {
                Text(
                    text = "Register",
                    fontSize = 30.sp)
            }
            Row {
                Text(text = "Already have an account?")
//                ClickableText() { }
            }

        }
    }
}//end RegistrationScreen

@Composable
private fun InputField(label: String, value: String, isPassword: Boolean = false, onValueChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Enter $label", color = Color.White) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Pink40, unfocusedContainerColor = Pink40,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}//end InputField