package com.example.blabapp.Nav

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

interface UserRepository{

    suspend fun getUser(uid: String): User
    suspend fun getUserUsingToObj(uid:String): User
}


class AccountRepository(private var fireStoreDb : FirebaseFirestore) {

    //val currentUser = getUser(FirebaseAuth.getInstance().uid.toString())

    suspend fun getUser(uid:String): User{
        var user:User
        val userDoc = fireStoreDb.collection("users").document(uid).get().await()

        if(userDoc!=null){
            val userData = userDoc.data
            if(userData != null) {
                user = User(
                    name = userData["name"].toString(),
                    email = userData["email"].toString(),
                    chatList = userData["chatList"] as MutableList<String>,
                    friendList = userData["friendList"] as MutableList<String>,
                    rank = userData["rank"] as Int,
                    userBio = userData["userBio"].toString(),
                    userId = userData["userId"].toString()
                )
            }
            else{
                user = User()
            }
        }
        else{
            user = User()
        }
        return user
    }

    suspend fun getUserUsingToObj(uid:String): User{

        val user = fireStoreDb.collection("users").document(uid).get().await()
            .toObject<User>()

        return user ?: User()

    }
}