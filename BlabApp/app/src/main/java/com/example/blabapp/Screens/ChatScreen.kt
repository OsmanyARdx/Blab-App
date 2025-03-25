package com.example.blabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Nav.BlabApp
import com.example.blabapp.ui.theme.BlabPurple
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Message(val sender: String, val content: String, var isRead: Boolean = false)

@Composable
fun ChatScreen(navController: NavHostController, contactName: String, accountRepository: AccountRepository) {
    val messages = remember { mutableStateListOf<Message>() }
    var newMessage by remember { mutableStateOf(TextFieldValue("")) }
    val isSidebarVisible = remember { mutableStateOf(false) }


    // Placeholder: load messages from Firebase
    LaunchedEffect(contactName) {
        loadMessagesFromFirebase(accountRepository.currentUser.userId, contactName) { loadedMessages ->
            messages.clear()
            messages.addAll(loadedMessages)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(text = contactName, fontSize = 20.sp, color = Color.White)
        }

        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }

        // message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier
                    .weight(1f),
                placeholder = { Text("Type a message...") }
            )
            IconButton(
                onClick = {
                    if (newMessage.text.isNotEmpty()) {
                        sendMessageToFirebase("You", newMessage.text, contactName)
                        newMessage = TextFieldValue("")
                    }
                },
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isUser = message.sender == "You"
    val bubbleColor = if (isUser) BlabPurple else Color.Yellow
    val textColor = if (isUser) Color.White else Color.Black
    val alignment = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = alignment,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isUser) {
            UserImage()
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .background(bubbleColor, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(text = message.content, fontSize = 16.sp, color = textColor)
        }

        if (isUser) {
            UserImage()
        }
    }
}

@Composable
fun UserImage() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Color.Gray, shape = CircleShape)
            .padding(4.dp)
    ) {
        // Placeholder for user image
    }
}

// Placeholder: load messages from Firebase
fun loadMessagesFromFirebase(
    currentUser: String,
    contactName: String,
    onMessagesLoaded: (List<Message>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("messages")
        .whereIn("sender", listOf(currentUser, contactName)) // Messages where sender is either user
        .whereIn("receiver", listOf(currentUser, contactName)) // Messages where receiver is either user
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .get()
        .addOnSuccessListener { result ->
            val messages = result.map { document ->
                Message(
                    sender = document.getString("sender") ?: "",
                    content = document.getString("content") ?: "",
                    isRead = document.getBoolean("isRead") ?: false
                )
            }
            onMessagesLoaded(messages)
        }
        .addOnFailureListener {
            onMessagesLoaded(emptyList()) // Handle failure
        }
}


// Placeholder: send messages to Firebase
fun sendMessageToFirebase(sender: String, content: String, contactName: String) {
    // TODO: Replace with actual Firebase  logic
}
