package com.example.blabapp.ViewModels

import androidx.lifecycle.ViewModel
import com.example.blabapp.Nav.AccountRepository


class RegisterScreenViewModel(private var accountRepository: AccountRepository): ViewModel() {
    fun registerUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email or password cannot be empty")
            return
        }
        else {
            onResult(true, "Registration successful!")
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