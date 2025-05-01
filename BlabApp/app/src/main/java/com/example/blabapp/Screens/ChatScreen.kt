package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.R
import com.example.blabapp.ViewModels.ChatScreenViewModel
import com.example.blabapp.ViewModels.MessagesScreenViewModel
import com.example.blabapp.ui.theme.BlabPurple
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch


data class Message(
    val senderId: String,
    val message: String,
    val read: Boolean = false,
    val timestamp: Timestamp
)




@Composable
fun ChatScreen(navController: NavHostController, chatRoomId: String, currentUserId: String, otherUserImageUrl: String, thisUserImageUrl: String, otherUserName: String ) {
    val viewModel = viewModel { ChatScreenViewModel()}
    val messages = viewModel.messages
    var newMessage by remember { mutableStateOf(TextFieldValue("")) }
    val userNames = remember { mutableStateMapOf<String, String>() } // To store userId and userName
    val lazyColumnListState = rememberLazyListState()


    // Load messages when the chat screen is displayed
    LaunchedEffect(chatRoomId) {
        viewModel.observeChatMessages(chatRoomId)
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyColumnListState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.surface
                )
            }

            Text(
                text = otherUserName,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.surface
            )
        }

        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp, vertical = 0.dp),
            state = lazyColumnListState

        ) {
            items(messages) { message ->
                val senderName = userNames[message.senderId] ?: "Unknown User"  // Default to "Unknown User" if name not found
                ChatBubble(message, senderName, message.senderId == currentUserId, thisUserImageUrl, otherUserImageUrl)
            }
        }

        // Message Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,

                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            IconButton(
                onClick = {
                    if (newMessage.text.isNotEmpty()) {
                        viewModel.sendMessageToFirebase(currentUserId, newMessage.text, chatRoomId)
                        newMessage = TextFieldValue("")  // Clear input field after sending
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.surface)
            }
        }
    }

    // Placeholder: Load user names for the chat
    fun loadUserNames(userIds: List<String>) {
        val db = FirebaseFirestore.getInstance()
        userIds.forEach { userId ->
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("name") ?: "Unknown User"
                    userNames[userId] = userName
                }
        }
    }

    // Fetch user names from the chat members
    LaunchedEffect(messages) {
        val userIds = messages.map { it.senderId }.toSet()  // Get unique user IDs from the messages
        loadUserNames(userIds.toList())
    }
}

@Composable
fun ChatBubble(message: Message, senderName: String, isUser: Boolean, thisUserImageUrl: String, otherUserImageUrl: String) {
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val textColor = if (isUser) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.tertiary
    val alignment = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = alignment,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isUser) {
            UserImage(otherUserImageUrl)
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .background(bubbleColor, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(text = message.message, fontSize = 16.sp, color = textColor)
        }

        if (isUser) {
            Log.d("pfpURL", thisUserImageUrl)
            UserImage(thisUserImageUrl)
        }
    }
}

@Composable
fun UserImage(profileImageUrl: String) {
    if (profileImageUrl.isNotEmpty()) {
        Image(
            painter = rememberAsyncImagePainter(profileImageUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentScale = ContentScale.Crop
        )
    } else {

        Image(
            painter = painterResource(id = R.drawable.default_profile_photo),
            contentDescription = "Default Profile Picture",
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}