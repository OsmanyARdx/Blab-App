package com.example.blabapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Screens.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessagesScreenViewModel(private var accountRepository: AccountRepository): ViewModel() {
    private val _conversations = MutableStateFlow<List<Message>>(emptyList())
    val conversations: StateFlow<List<Message>> get() = _conversations

    fun fetchConversations() {
        accountRepository.loadConversations() { fetchedConversations ->
            _conversations.value = fetchedConversations
        }
    }
}
