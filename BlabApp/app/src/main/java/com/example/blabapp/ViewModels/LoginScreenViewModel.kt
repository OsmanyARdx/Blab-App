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
            val userStreak = document.getLong("userStreak") ?: 1

            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

            val lastLoginCal = Calendar.getInstance()
            lastLoginTimestamp?.toDate()?.let { lastLoginCal.time = it }

            val isToday =
                lastLoginCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        lastLoginCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

            val isYesterday =
                lastLoginCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        lastLoginCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)

            val newStreak = when {
                isToday -> userStreak // Same day, no change
                isYesterday -> userStreak + 1 // Continue streak
                else -> 1 // Missed a day, reset to 1
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
