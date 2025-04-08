package com.example.blabapp.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Repository.ChatRepository
import com.example.blabapp.Screens.Message

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class ChatScreenViewModel(): ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> get() = _messages

    private var listenerRegistration: ListenerRegistration? = null

    fun observeChatMessages(chatRoomId: String) {
        // Clean up previous listener
        listenerRegistration?.remove()

        listenerRegistration = db.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timeCreated")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed: ", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val newMessages = snapshot.documents.mapNotNull { doc ->
                        val senderId = doc.getString("senderId")
                        val message = doc.getString("message")
                        val read = doc.getBoolean("read") ?: false
                        val timeCreated = doc.getDate("timeCreated")?.toString() ?: ""

                        if (senderId != null && message != null) {
                            Message(senderId, message, read, timeCreated)
                        } else null
                    }
                    _messages.clear()
                    _messages.addAll(newMessages)
                }
            }
    }
    // Function to send a message to Firebase
    fun sendMessageToFirebase(senderId: String, content: String, chatRoomId: String) {
        val db = FirebaseFirestore.getInstance()

        val message = hashMapOf(
            "senderId" to senderId,
            "message" to content,
            "timeCreated" to Timestamp.now(),  // Use timestamp to sort messages
            "read" to false
        )

        db.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Message sent successfully
                Log.d("DB", "s")
            }
            .addOnFailureListener {
                // Handle error
                Log.d("DB", "f")
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

