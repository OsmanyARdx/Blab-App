package com.example.blabapp

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.ViewModels.MessagesScreenViewModel
import com.example.blabapp.ViewModels.RegisterScreenViewModel
import com.example.blabapp.ViewModels.WordleViewModel
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.Purple40
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun MessagesScreen(navController: NavHostController, accountRepository: AccountRepository) {
    // Sample conversations (Replace with Firebase data)
    val viewModel = viewModel { MessagesScreenViewModel(accountRepository) }
    val conversations by viewModel.chatrooms.collectAsState()

    // Fetch conversations when the screen is loaded
    LaunchedEffect(Unit) {
        viewModel.loadChatrooms()
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Messages",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Message List
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(conversations) { conversation ->
                    Log.d(
                        "ChatroomPreview", "Chatroom ID: ${conversation.chatroomId}, " +
                                "Other User ID: ${conversation.otherUserId}, " +
                                "Other User Name: ${conversation.otherUserName}, " +
                                "Other User Image: ${conversation.otherUserImage}, " +
                                "Last Message: ${conversation.lastMessage}"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                val currentEncodedUrl = URLEncoder.encode(conversation.currentUserImage, StandardCharsets.UTF_8.toString())
                                val otherEncodedUrl = URLEncoder.encode(conversation.otherUserImage, StandardCharsets.UTF_8.toString())
                                navController.navigate("ChatScreen/${conversation.chatroomId}/${conversation.currentUserId}/${currentEncodedUrl}/${otherEncodedUrl}") // navigate to Chat screen
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (conversation.otherUserImage.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(conversation.otherUserImage),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, BlabPurple, CircleShape)
                                    .background(BlabPurple),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile_photo),
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, BlabPurple, CircleShape)
                                    .background(BlabPurple)
                            )
                        }

                        // Message text content
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = conversation.otherUserName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = conversation.lastMessage,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, // Bold if unread
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
