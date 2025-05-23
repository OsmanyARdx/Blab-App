package com.example.blabapp.Screens

import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.blabapp.ui.theme.BlabLight
import com.example.blabapp.ui.theme.BlabPurple

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

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState), // 🔽 Make content scrollable
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
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
                    InputField(
                        "Confirm Password",
                        confirmPassword,
                        isPassword = true
                    ) { confirmPassword = it }

                    // Language selection with two buttons
                    Text(
                        "I want to learn:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Button(
                            onClick = { selectedLanguage = "EN" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLanguage == "EN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                "English", fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Button(
                            onClick = { selectedLanguage = "ES" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLanguage == "ES") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                "Spanish", fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(4.dp)
                    ) {
                    Button(
                        onClick = {
                            when {
                                firstName.isEmpty() || lastName.isEmpty() -> showToast(
                                    context,
                                    "Name fields cannot be empty"
                                )

                                email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email)
                                    .matches() -> showToast(context, "Enter a valid email")

                                phone.isEmpty() || !phone.matches(Regex("^\\d{10}$")) -> showToast(
                                    context,
                                    "Enter a valid 10-digit phone number"
                                )

                                password.length < 6 -> showToast(
                                    context,
                                    "Password must be at least 6 characters"
                                )

                                password != confirmPassword -> showToast(
                                    context,
                                    "Passwords do not match"
                                )

                                selectedLanguage == null -> showToast(
                                    context,
                                    "Please select a language"
                                ) // Language selection check
                                else -> {
                                    viewModel.registerUserFirebase(
                                        email = email,
                                        password = password,
                                        name = "$firstName $lastName",
                                        imageUrl = "",
                                        learning = selectedLanguage!!, // Use selectedLanguage with non-null assertion
                                        successfulRegistrationHandler = {
                                            Toast.makeText(
                                                context,
                                                "Registration successful!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("LoginScreen")
                                        },
                                        unsuccessfulRegistrationHandler = { error ->
                                            Toast.makeText(
                                                context,
                                                "Registration failed: $error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .border(
                                3.dp,
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(50.dp)
                            ),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(text = "Register", fontSize = 20.sp)
                    }
                }

                    Row {

                        Text(
                            text = "Already have an account?",
                            color = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Login",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { navController.navigate("loginScreen") }
                        )
                    }
                }
            }
        }
    }
}
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }