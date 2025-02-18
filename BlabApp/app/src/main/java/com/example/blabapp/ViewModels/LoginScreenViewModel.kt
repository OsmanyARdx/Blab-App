package com.example.blabapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginScreenViewModel(private var accountRepository: AccountRepository): ViewModel() {
    fun loginFirebase(email: String,
                      password: String,
                      successfulLoginHandler: ()->Unit,
                      unsuccessfulLoginHandler: ()->Unit){

        var firebaseAuth = FirebaseAuth.getInstance()
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                successfulLoginHandler()
            } catch (e: Exception) {
                unsuccessfulLoginHandler()
            }
        }
    }
}