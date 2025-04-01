package com.example.blabapp.Nav


import com.example.blabapp.Screens.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

interface UserRepository{

    suspend fun getUser(uid: String): User
}


class AccountRepository(private var fireStoreDb : FirebaseFirestore): UserRepository{

    lateinit var currentUser: User

    override suspend fun getUser(uid:String): User{

        val user = fireStoreDb.collection("users").document(uid).get().await()
            .toObject<User>()

        return user ?: User()

    }





    fun loadConversations(onConversationsLoaded: (List<Message>) -> Unit) {
        fireStoreDb.collection("messages")
                .whereIn("sender", listOf(FirebaseAuth.getInstance().uid.toString())) // Fetch only messages sent by the user
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by recent messages
                .get()
                .addOnSuccessListener { result ->
                    val messages = result.map { document ->
                        Message(
                            sender = document.getString("sender") ?: "",
                            content = document.getString("content") ?: "",
                            isRead = document.getBoolean("isRead") ?: false
                        )
                    }.distinctBy { it.sender } // Get only unique senders
                    onConversationsLoaded(messages)
                }
                .addOnFailureListener {
                    onConversationsLoaded(emptyList()) // Handle failure
                }
        }
}

    /*
    override suspend fun getUser(uid:String): User{
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
    */

