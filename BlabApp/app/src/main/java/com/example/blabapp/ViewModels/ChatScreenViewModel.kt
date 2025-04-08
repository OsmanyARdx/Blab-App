package com.example.blabapp.ViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Repository.ChatRepository
import com.example.blabapp.Screens.Message

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class ChatScreenViewModel(): ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    fun loadChatLogs(chatRoomId: String) {
        viewModelScope.launch {
            _messages.value = ChatRepository.loadMessagesFromFirebase(chatRoomId)
        }
    }
}

