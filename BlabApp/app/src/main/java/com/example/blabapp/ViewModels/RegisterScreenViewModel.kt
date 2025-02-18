package com.example.blabapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class RegisterScreenViewModel(private var accountRepository: AccountRepository): ViewModel() {

    fun registerUserFirebase(
        email: String,
        password: String,
        successfulRegistrationHandler: () -> Unit,
        unsuccessfulRegistrationHandler: (String) -> Unit
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                successfulRegistrationHandler()
            } catch (e: Exception) {
                unsuccessfulRegistrationHandler(e.message ?: "Unknown error occurred")
            }
        }
    }
}