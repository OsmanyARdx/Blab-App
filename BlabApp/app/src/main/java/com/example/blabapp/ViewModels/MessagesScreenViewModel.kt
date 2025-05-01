package com.example.blabapp.ViewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Nav.User
import com.example.blabapp.Repository.ChatRepository
import com.example.blabapp.Repository.ChatroomPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesScreenViewModel(private var accountRepository: AccountRepository): ViewModel() {


    private val _chatrooms = MutableStateFlow<List<ChatroomPreview>>(emptyList())
    val chatrooms = _chatrooms.asStateFlow()


    var isLoading = mutableStateOf(true)
        private set

    fun loadChatrooms() {
        viewModelScope.launch {
            isLoading.value = true
            _chatrooms.value = ChatRepository.getUserChatrooms()

            isLoading.value = false
        }

    }
}
