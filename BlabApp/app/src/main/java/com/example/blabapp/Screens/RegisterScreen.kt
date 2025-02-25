package com.example.blabapp.Screens

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.blabapp.Design.InputField
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.ViewModels.RegisterScreenViewModel
import com.example.blabapp.ui.theme.BlabBlue
import com.example.blabapp.ui.theme.BlabGreen
import com.example.blabapp.ui.theme.BlabGrey
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow


@Composable
fun RegisterScreen(accountRepository: AccountRepository, navController: NavController){
    val viewModel = viewModel { RegisterScreenViewModel(accountRepository) }
    var rememberEmail by rememberSaveable { mutableStateOf("") }
    var rememberPassword by rememberSaveable { mutableStateOf("") }
    var success by rememberSaveable {
        mutableStateOf(true)
    }

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
            .background(BlabYellow),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = BlabPurple)

            InputField("First Name", firstName) { firstName = it }
            InputField("Last Name", lastName) { lastName = it }
            InputField("Email Address", email) { email = it }
            InputField("Phone Number", phone) { phone = it }
            InputField("Password", password, isPassword = true) { password = it }
            InputField("Confirm Password", confirmPassword, isPassword = true) { confirmPassword = it }

            Button(
                onClick = {
                    when {
                        firstName.isEmpty() || lastName.isEmpty() ->
                            showToast(context, "Name fields cannot be empty")
                        email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                            showToast(context, "Enter a valid email")
                        phone.isEmpty() || phone.length < 10 ->
                            showToast(context, "Enter a valid phone number")
                        password.length < 6 ->
                            showToast(context, "Password must be at least 6 characters")
                        password != confirmPassword ->
                            showToast(context, "Passwords do not match")
                        else -> {
                            viewModel.registerUserFirebase(
                                email = email,
                                password = password,
                                name = firstName + " " + lastName,
                                imageUrl = "",
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
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = BlabBlue, contentColor = BlabYellow)
            ) {
                Text(text = "Register", fontSize = 20.sp)
            }
            Row {
                Text(text = "Already have an account?")
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Login",
                    color = BlabPurple,
                    modifier = Modifier.clickable { navController.navigate("loginScreen") }
                )
            }
        }
    }
}



private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}