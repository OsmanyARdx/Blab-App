package com.example.blabapp.Repository

import android.util.Log
import com.example.blabapp.Screens.Message

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserChatrooms(): List<ChatroomPreview> {
        val user = UserRepository.getUser()
        val userId = user!!.userId
        val userImage = user.imageUrl
        val userDoc = db.collection("users").document(userId).get().await()

        val chatList = userDoc.get("chatList") as? List<String> ?: emptyList()
        val chatrooms = mutableListOf<ChatroomPreview>()

        for (chatroomId in chatList) {
            val chatroomDoc = db.collection("chatRooms").document(chatroomId).get().await()
            if (!chatroomDoc.exists()) continue

            val members = chatroomDoc.get("members") as? List<String> ?: emptyList()
            val otherUserId = members.find { it != userId } ?: continue

            // Fetch other user's details
            val otherUserDoc = db.collection("users").document(otherUserId).get().await()
            val otherUserName = otherUserDoc.getString("name") ?: "Unknown"
            val otherUserImage = otherUserDoc.getString("imageUrl") ?: ""

            // Fetch last message
            val lastMessageQuery = db.collection("chatRooms")
                .document(chatroomId)
                .collection("messages")
                .orderBy("timeCreated") // Assuming messages have a 'timestamp' field
                .limitToLast(1)
                .get()
                .await()

            val lastMessage = lastMessageQuery.documents.firstOrNull()?.getString("message") ?: "No messages"

            chatrooms.add(ChatroomPreview(chatroomId, otherUserId, otherUserName, otherUserImage, lastMessage, userId, userImage))
        }

        return chatrooms
    }
    suspend fun loadMessagesFromFirebase(chatRoomId: String): List<Message> {
        val db = FirebaseFirestore.getInstance()
        val chatRoomRef = db.collection("chatRooms").document(chatRoomId)
        val messagesList = mutableListOf<Message>()

        chatRoomRef.get().addOnSuccessListener { chatRoomSnapshot ->
            if (chatRoomSnapshot.exists()) {
                val members = chatRoomSnapshot.get("members") as? List<String> ?: listOf()
                Log.d("FirebaseChat", "Chatroom ID: $chatRoomId, Members: $members")

                // Fetch messages from the "messages" subcollection
                chatRoomRef.collection("messages")
                    .orderBy("timeCreated", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener { messagesSnapshot ->


                        for (document in messagesSnapshot) {
                            val message = document.getString("message") ?: "No message"
                            val read = document.getBoolean("read") ?: false
                            val senderId = document.getString("senderId") ?: "Unknown sender"
                            val timeCreated = document.getTimestamp("timeCreated")

                            val msg = Message(senderId,message,read, timeCreated!!)
                            messagesList.add(msg)

                            Log.d("FirebaseChat", "Message: ${msg.message}, Read: ${msg.read}, Sender: ${msg.senderId}, Time Created: ${msg.timestamp}")
                        }

                        // Use messagesList as needed (e.g., update UI, pass to a RecyclerView adapter, etc.)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirebaseChat", "Error loading messages", exception)
                    }
            } else {
                Log.e("FirebaseChat", "Chatroom ID: $chatRoomId not found")
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseChat", "Error loading chatroom", exception)
        }
        return messagesList
    }
}


data class ChatroomPreview(
    val chatroomId: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserImage: String,
    val lastMessage: String,
    val currentUserId: String,
    val currentUserImage: String
) {
}

