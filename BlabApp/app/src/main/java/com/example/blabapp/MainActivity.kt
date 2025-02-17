package com.example.blabapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blabapp.ui.theme.BlabAppTheme
import com.example.blabapp.ui.theme.Pink40
import com.example.blabapp.ui.theme.Pink80

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlabAppTheme {
                val navController = rememberNavController() // Initialize NavController

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splashScreen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splashScreen") {
                            SplashScreen(navController = navController)
                        }
                        composable("startupScreen") {
                            StartupScreen(navController = navController)
                        }
                        composable("loginScreen") {
                            LoginScreen(navController = navController)
                        }
                        composable("registrationScreen") {
                            RegistrationScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(Pink80),
        contentAlignment = Alignment.Center
    ) {
        Text("Blab App", fontSize = 50.sp, color = Color.White)
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
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
        modifier = Modifier
            .fillMaxSize()
            .background(Pink80),
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
                    // Handle the login logic here (e.g., Firebase login)
                    Toast.makeText(context, "Logged in with: $email", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Pink40
                )
            ) {
                Text(
                    text = "Login",
                    fontSize = 30.sp
                )
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
private fun RegistrationScreen(navController: NavController) {
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
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Log in",
                    color = Color.Blue,
                    modifier = Modifier.clickable { navController.navigate("LoginScreen") } )

            }

        }
    }
}//end RegistrationS

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
