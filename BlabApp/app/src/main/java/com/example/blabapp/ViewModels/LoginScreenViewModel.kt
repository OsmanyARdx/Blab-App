package com.example.blabapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class LoginScreenViewModel(private var accountRepository: AccountRepository): ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun loginFirebase(
        email: String,
        password: String,
        successfulLoginHandler: () -> Unit,
        unsuccessfulLoginHandler: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                // Update last login and streak
                updateLoginStreak(userId)

                UserRepository.refreshUser()

                successfulLoginHandler()
            } catch (e: Exception) {
                unsuccessfulLoginHandler()
            }
        }
    }

    private suspend fun updateLoginStreak(userId: String) {
        val userRef = firestore.collection("users").document(userId)

        val document = userRef.get().await()
        if (document.exists()) {
            val lastLoginTimestamp = document.getTimestamp("lastLogin")
            val userStreak = document.getLong("userStreak") ?: 0

            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

            val lastLoginCal = Calendar.getInstance()
            lastLoginTimestamp?.toDate()?.let { lastLoginCal.time = it }

            val newStreak = if (
                lastLoginCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                lastLoginCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
            ) {
                userStreak + 1 // Continue the streak
            } else {
                1 // Reset the streak
            }

            // Update Firestore
            userRef.update(
                mapOf(
                    "userStreak" to newStreak,
                    "lastLogin" to Timestamp.now()
                )
            ).await()
        }
    }
}
