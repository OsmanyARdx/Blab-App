package com.example.blabapp.Nav


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

interface UserRepository{

    suspend fun getUser(uid: String): User
}


class AccountRepository(private var fireStoreDb : FirebaseFirestore): UserRepository{

    override suspend fun getUser(uid:String): User{

        val user = fireStoreDb.collection("users").document(uid).get().await()
            .toObject<User>()

        return user ?: User()

    }
}
