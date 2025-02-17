package com.example.blabapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.blabapp.ui.theme.Pink40
import com.example.blabapp.ui.theme.Pink80
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(Pink80),
        contentAlignment = Alignment.Center
    ) {
        Text("Blab App", fontSize = 50.sp, color = Color.White)
    }

    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("startupScreen") // After splash screen, navigate to startup
    }
}

@Composable
fun StartupScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(Pink80),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to Blab App", fontSize = 30.sp, color = Color.White)

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = { navController.navigate("loginScreen") },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = Pink40)
            ) {
                Text(text = "Login", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("registrationScreen") },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = Pink40)
            ) {
                Text(text = "Register", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
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
                    modifier = Modifier.clickable { navController.navigate("registrationScreen") }
                )
            }
        }
    }
}

@Composable
fun RegistrationScreen(navController: NavController) {
    val context = LocalContext.current
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Pink80),
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
                colors = ButtonDefaults.buttonColors(containerColor = Pink40)
            ) {
                Text(text = "Register", fontSize = 30.sp)
            }

            Row {
                Text(text = "Already have an account?")
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Log in",
                    color = Color.Blue,
                    modifier = Modifier.clickable { navController.navigate("LoginScreen") }
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
}
