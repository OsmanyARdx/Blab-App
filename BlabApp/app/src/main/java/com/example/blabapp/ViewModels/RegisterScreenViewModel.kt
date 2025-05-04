package com.example.blabapp.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class RegisterScreenViewModel(private var accountRepository: AccountRepository): ViewModel() {

    fun registerUserFirebase(
        email: String,
        password: String,
        name: String,
        imageUrl: String,
        userBio: String = "My description",
        userStreak: Int = 0,
        userRank: String = "Simple Student",
        learning: String,
        chatList: MutableList<String> = mutableListOf<String>(),
        friendList: MutableList<String> = mutableListOf<String>(),
        completeMod: MutableList<String> = mutableListOf<String>(),
        successfulRegistrationHandler: () -> Unit,
        unsuccessfulRegistrationHandler: (String) -> Unit
    ) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            try {
                // Create user in Firebase Authentication
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    val userData = mapOf(
                        "name" to name,
                        "userId" to userId,
                        "email" to email,
                        "imageUrl" to imageUrl,
                        "userBio" to userBio,
                        "userStreak" to userStreak,
                        "userRank" to userRank,
                        "learning" to learning,
                        "chatList" to chatList,
                        "friendList" to friendList,
                        "lastLogin" to com.google.firebase.Timestamp.now(),
                        "completeMod" to completeMod,
                        "friendCode" to generateUniqueFriendCode(firestore)
                    )

                    firestore.collection("users")
                        .document(userId)
                        .set(userData)
                        .await()

                    successfulRegistrationHandler()
                } else {
                    unsuccessfulRegistrationHandler("Failed to get user ID.")
                }
            } catch (e: Exception) {
                Log.e("RegisterUser", "Error during registration: ${e.message}", e) // Log detailed error
                unsuccessfulRegistrationHandler(e.message ?: "Unknown error occurred")
            }
        }
    }


    suspend fun generateUniqueFriendCode(db: FirebaseFirestore): String{
        Log.d("generateUniqueFriendCode", "start")
        val min = 1000000000L
        val max = 9999999999L
        var friendCode = Random.nextLong(min,max+1).toString()
        var tryAgain = true

        while(tryAgain){
            Log.d("generateUniqueFriendCode", "top of loop")

            Log.d("generateUniqueFriendCode", friendCode)

            if(db.collection("users").whereEqualTo("friendCode", friendCode).get().await().isEmpty){
                Log.d("generateUniqueFriendCode", "found unique")
                tryAgain = false
            }
            else{
                Log.d("generateUniqueFriendCode", "generate new code")
                friendCode = Random.nextLong(min,max+1).toString()
                Log.d("generateUniqueFriendCode", friendCode)
            }

        }
        Log.d("generateUniqueFriendCode", "end")
        return friendCode
    }

    fun updateUserFields(
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update(updates)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                Log.e("UpdateUserFields", "Error updating fields: ${e.message}", e)
                onFailure(e.message ?: "Unknown error occurred")
            }
        }
    }

}
