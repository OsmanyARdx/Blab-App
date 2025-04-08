package com.example.blabapp.Repository

import com.example.blabapp.Nav.User

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

object UserRepository {

    private val fireStoreDb = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var cachedUser: User? = null

    suspend fun getUser(): User? {
        val userId = firebaseAuth.currentUser?.uid ?: return null
        return cachedUser ?: fetchUserFromFirestore(userId).also { cachedUser = it }
    }

    suspend fun refreshUser(): User? {
        val userId = firebaseAuth.currentUser?.uid ?: return null
        return fetchUserFromFirestore(userId).also { cachedUser = it }
    }

    private suspend fun fetchUserFromFirestore(uid: String): User {
        val document = fireStoreDb.collection("users").document(uid).get().await()
        return document.toObject<User>() ?: User()
    }

}