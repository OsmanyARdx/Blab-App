package com.example.blabapp.Screens

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.blabapp.Design.InputField
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.R
import com.example.blabapp.ViewModels.RegisterScreenViewModel
import com.example.blabapp.ui.theme.BlabBlue
import com.example.blabapp.ui.theme.BlabGreen
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import com.example.blabapp.ui.theme.DarkBlabBlue


@Composable
fun RegisterScreen(accountRepository: AccountRepository, navController: NavController) {
    val viewModel = viewModel { RegisterScreenViewModel(accountRepository) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val logoPic = painterResource(R.drawable.logo)


    var selectedLanguage by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.Transparent, shape = RoundedCornerShape(75.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = logoPic,
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }
            InputField("First Name", firstName) { firstName = it }
            InputField("Last Name", lastName) { lastName = it }
            InputField("Email Address", email) { email = it }
            InputField("Phone Number", phone) { phone = it }
            InputField("Password", password, isPassword = true) { password = it }
            InputField("Confirm Password", confirmPassword, isPassword = true) { confirmPassword = it }


            // Language selection with two buttons
            Text("I want to learn:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Button(
                    onClick = { selectedLanguage = "EN" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLanguage == "EN") BlabPurple else MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("English", fontSize = 16.sp)
                }

                Button(
                    onClick = { selectedLanguage = "ES" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLanguage == "ES") BlabPurple else MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Spanish", fontSize = 16.sp)
                }
            }

            Button(
                onClick = {
                    when {
                        firstName.isEmpty() || lastName.isEmpty() -> showToast(context, "Name fields cannot be empty")
                        email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showToast(context, "Enter a valid email")
                        phone.isEmpty() || phone.length < 10 -> showToast(context, "Enter a valid phone number")
                        password.length < 6 -> showToast(context, "Password must be at least 6 characters")
                        password != confirmPassword -> showToast(context, "Passwords do not match")
                        selectedLanguage == null -> showToast(context, "Please select a language") // Language selection check
                        else -> {
                            viewModel.registerUserFirebase(
                                email = email,
                                password = password,
                                name = "$firstName $lastName",
                                imageUrl = "",
                                learning = selectedLanguage!!, // Use selectedLanguage with non-null assertion
                                successfulRegistrationHandler = {
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("LoginScreen")
                                },
                                unsuccessfulRegistrationHandler = { error ->
                                    Toast.makeText(context, "Registration failed: $error", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                },

                modifier = Modifier
                    .size(150.dp, 50.dp)
                    .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(50.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.Black)
            ) {
                Text(text = "Register", fontSize = 20.sp)
            }

            Row {

                Text(text = "Already have an account?",
                    color = BlabPurple)

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Login",
                    color = BlabGreen,
                    modifier = Modifier.clickable { navController.navigate("loginScreen") }
                )
            }
        }
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}