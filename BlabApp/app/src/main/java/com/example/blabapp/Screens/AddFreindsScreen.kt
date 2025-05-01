package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.R
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.Repository.UserRepository.refreshUser
import com.example.blabapp.ui.theme.BlabPurple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AddFriendsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = remember { mutableStateOf<List<User>>(emptyList()) }
    val isSidebarVisible = remember { mutableStateOf(false) }
    val profileImageUrl = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var currentFriendList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentUserFriendCode by remember { mutableStateOf("") }

        LaunchedEffect(searchQuery) {
        coroutineScope.launch {
            val user = UserRepository.getUser()
            user?.let {
                profileImageUrl.value = it.imageUrl ?: ""
                currentFriendList = it.friendList ?: emptyList()
                currentUserFriendCode = it.friendCode
            }
        }

        if (searchQuery.isNotEmpty()) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users")
                .whereEqualTo("friendCode", searchQuery)
                .get()
                .addOnSuccessListener { result ->
                    val users = mutableListOf<User>()
                    for (document in result) {
                        val userName = document.getString("name")
                        val userId = document.id
                        if (userName != null && userId != currentUserId) {
                            users.add(User(userName, userId))
                        }
                    }
                    filteredUsers.value = users
                }
                .addOnFailureListener { }
        } else {
            filteredUsers.value = emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(7.dp)
            ) {
                IconButton(
                    onClick = { isSidebarVisible.value = !isSidebarVisible.value },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    if (profileImageUrl.value.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUrl.value),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
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
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Text(
                    text = "Add Friends",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.align(Alignment.Center).padding(top = 16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Your Friend Code:",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = currentUserFriendCode,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search for friends") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredUsers.value) { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user.name, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                                if (!currentFriendList.contains(user.userId)) {
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            addFriend(user.userId, navController)
                                        }
                                    }) {
                                        Text("Add")
                                    }
                                }else{
                                    Text(
                                        "Already Friends",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("friends_list") },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(.7f)
                                .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp)),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text("Back to Friends", fontSize = 25.sp)
                        }
                    }
                }
            }
        }

        if (isSidebarVisible.value) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            onClick = { isSidebarVisible.value = false },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                )

                SidebarMenu(
                    navController = navController,
                    isVisible = isSidebarVisible.value,
                    onDismiss = { isSidebarVisible.value = false }
                )
            }
        }
    }
}

suspend fun requestFriend(userId: String, navController: NavController){
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUserId = firebaseAuth.currentUser?.uid ?: return
    firestore.collection("users").document(userId)
        .update("friendRequests", FieldValue.arrayUnion(currentUserId))
        .addOnSuccessListener { Log.d("Request Status", "Friend Request Sent")}
        .addOnFailureListener { Log.d("Request Status", "Friend Request Failed")}

    delay(500) // Let Firestore sync up before refreshing

    UserRepository.refreshUser()
    navController.popBackStack()
}

suspend fun addFriend(userId: String, navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUserId = firebaseAuth.currentUser?.uid ?: return

    firestore.collection("users").document(currentUserId)
        .update("friendList", FieldValue.arrayUnion(userId))
        .addOnSuccessListener {
            createChatRoom(currentUserId, userId)
        }
        .addOnFailureListener { }

    delay(500) // Let Firestore sync up before refreshing

    UserRepository.refreshUser()
    navController.popBackStack()
}

fun createChatRoom(currentUserId: String, friendUserId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val chatRoomData = hashMapOf("members" to listOf(currentUserId, friendUserId))

    firestore.collection("chatRooms")
        .add(chatRoomData)
        .addOnSuccessListener { documentReference ->
            val chatRoomId = documentReference.id
            firestore.collection("chatRooms").document(chatRoomId)
                .update("chatRoomId", chatRoomId)
                .addOnSuccessListener {
                    //initializeMessagesSubcollection(chatRoomId)
                    updateUserChatList(currentUserId, friendUserId, chatRoomId)
                }
        }
}

fun updateUserChatList(userId: String, friendUserId: String, chatRoomId: String) {
    FirebaseFirestore.getInstance().collection("users").document(userId)
        .update("chatList", FieldValue.arrayUnion(chatRoomId))
    FirebaseFirestore.getInstance().collection("users").document(friendUserId)
        .update("chatList", FieldValue.arrayUnion(chatRoomId))
}

fun initializeMessagesSubcollection(chatRoomId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val messageData = hashMapOf(
        "message" to "f",
        "read" to false,
        "senderId" to "",
        "timeCreated" to FieldValue.serverTimestamp()
    )

    firestore.collection("chatRooms").document(chatRoomId)
        .collection("messages")
        .add(messageData)
}

data class User(val name: String = "", val userId: String = "", val friendList: List<String>? = null)
