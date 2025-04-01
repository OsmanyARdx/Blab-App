package com.example.blabapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class Message(
    val message: String = "",
    val read: Boolean = false,
    val senderId: String = "",
    val timeCreated: Timestamp? = null
)

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    /*
    fun loadMessages(chatRoomId: String) {
        val chatRoomRef = db.collection("chatRooms").document(chatRoomId)
            .collection("messages")
            .orderBy("timeCreated", Query.Direction.ASCENDING)

        chatRoomRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("ChatViewModel", "Error loading messages", exception)
                return@addSnapshotListener
            }

            val messagesList = snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()

            viewModelScope.launch {
                _messages.emit(messagesList)
            }
            Log.d("ChatViewModel", "Loaded ${messagesList.size} messages")
        }
    }
    fun sendMessage(chatRoomId: String, messageText: String, senderId: String) {
        val message = Message(
            message = messageText,
            read = false,
            senderId = senderId,
            timeCreated = Timestamp.now()
        )

        db.collection("chatrooms").document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error sending message", e)
            }
    }

     */
}