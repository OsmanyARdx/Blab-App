package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.R
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.ui.theme.BlabPurple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FriendsListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val friendsList = remember { mutableStateOf<List<String>>(emptyList()) }
    val friendNames = remember { mutableStateOf<List<String>>(emptyList()) } // Holds the friend names
    val filteredFriends = friendNames.value.filter { it.contains(searchQuery, ignoreCase = true) }

    val currentUserId = remember {  mutableStateOf("") }
    val currentUserName = remember {  mutableStateOf("") }


    val requestNames = remember { mutableStateOf<List<String>>(emptyList()) } // Holds the request names
    val filteredRequest = requestNames.value.filter { it.contains(searchQuery, ignoreCase = true) }


    val isSidebarVisible = remember { mutableStateOf(false) }
    val profileImageUrl = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val requestList = remember { mutableStateOf<List<String>>(emptyList()) } // Holds incoming friend requests
    val requestDetails = remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch user profile info (like profile image URL)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val user = UserRepository.getUser()
            user?.let {
                currentUserId.value = it.userId ?: ""
                currentUserName.value = it.name ?: ""
                profileImageUrl.value = it.imageUrl ?: ""
            }
        }
    }

    // Fetch friend list and names from Firestore
    LaunchedEffect(Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Get the friendList field from the user document
                        val friends = document.get("friendList") as? List<String>

                        val request = document.get("requestList") as? List<String>
                        if (friends != null) {
                            friendsList.value = friends // Update the friends list state

                            // Query Firestore to get names for each friend in the list
                            val namesList = mutableListOf<String>()
                            var loadedCount = 0 // Counter to track when all names are loaded

                            friends.forEach { friendId ->
                                firestore.collection("users").document(friendId).get()
                                    .addOnSuccessListener { friendDoc ->
                                        val friendName = friendDoc.getString("name")
                                        if (friendName != null) {
                                            namesList.add(friendName)
                                        }

                                        // After fetching the name, check if all friends are loaded
                                        loadedCount++
                                        if (loadedCount == friends.size) {
                                            // Update the state and log once all names are loaded
                                            friendNames.value = namesList
                                            Log.d("Namelist", namesList.toString()) // Log the names after all are loaded
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("FriendNameError", "Failed to fetch friend name for ID: $friendId")
                                    }
                            }
                        }

                        if (request != null) {
                            requestList.value = request // Update the friends list state

                            // Query Firestore to get names for each friend in the list
                            val requestNamesList = mutableListOf<String>()
                            var loadedCount = 0 // Counter to track when all names are loaded

                            request.forEach { requestId ->
                                firestore.collection("users").document(requestId).get()
                                    .addOnSuccessListener { friendDoc ->
                                        val requestName = friendDoc.getString("name")
                                        if (requestName != null) {
                                            requestNamesList.add(requestName)
                                        }

                                        // After fetching the name, check if all friends are loaded
                                        loadedCount++
                                        if (loadedCount == request.size) {
                                            // Update the state and log once all names are loaded
                                            requestNames.value = requestNamesList
                                            Log.d("Namelist", requestNamesList.toString()) // Log the names after all are loaded
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("FriendNameError", "Failed to fetch friend name for ID: $requestId")
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("FirestoreError", "Failed to fetch user document")
                }
        } else {
            // Handle case when the user is not authenticated
        }

}

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = BlabPurple,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
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
                                    .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile_photo),
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }

                    Text(
                        text = "Friends List",
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
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search for friends") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )


                        LazyColumn(modifier = Modifier.weight(1f)) {
                            // Requests Section
                            if (filteredRequest.isNotEmpty()) {
                                items(requestList.value.zip(filteredRequest)) { (requestId, requestName) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp, horizontal = 8.dp)
                                            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = requestName,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        addFriend(requestId, navController)
                                                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                                        currentUserId?.let {
                                                            FirebaseFirestore.getInstance().collection("users")
                                                                .document(it)
                                                                .update("requestList", FieldValue.arrayRemove(requestId))
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Text("Accept", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                            }

                                            Button(
                                                onClick = {
                                                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                                    currentUserId?.let {
                                                        FirebaseFirestore.getInstance().collection("users")
                                                            .document(it)
                                                            .update("requestList", FieldValue.arrayRemove(requestId))
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                                modifier = Modifier.padding(start = 4.dp)
                                            ) {
                                                Text("Decline", fontSize = 14.sp, color = Color.Black)
                                            }
                                        }
                                    }
                                }
                            }
                            val db = FirebaseFirestore.getInstance()
                            // Friends Section
                            if (filteredFriends.isNotEmpty()) {
                                items(friendsList.value.zip(filteredFriends)) { (friendId, friend) ->
                                    var friendImageUrl by remember { mutableStateOf("") }

                                    LaunchedEffect(Unit) {
                                        coroutineScope.launch {
                                            try {
                                                val userDoc = db.collection("users").document(currentUserId.value).get().await()
                                                val userImageUrl = userDoc.getString("imageUrl")

                                                if (userImageUrl != null) {
                                                    friendImageUrl = userImageUrl
                                                } else {
                                                }
                                            } catch (e: Exception) {
                                            }
                                        }
                                    }

                                    Row {
                                    Text(
                                        text = friend,
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
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
                                onClick = { navController.navigate("add_friends") },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth(.7f)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp)),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text("Add Friends", fontSize = 25.sp)
                            }
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



suspend fun addFriend(userId: String, navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUserId = firebaseAuth.currentUser?.uid ?: return

    val currentUserRef = firestore.collection("users").document(currentUserId)
    val otherUserRef = firestore.collection("users").document(userId)

    try {
        // Fetch both users' friend lists
        val currentUserDoc = currentUserRef.get().await()
        val otherUserDoc = otherUserRef.get().await()

        val currentUserFriends = currentUserDoc.get("friendList") as? List<*> ?: emptyList<Any>()
        val otherUserFriends = otherUserDoc.get("friendList") as? List<*> ?: emptyList<Any>()

        // Check if they're already friends
        val alreadyFriends = currentUserFriends.contains(userId) || otherUserFriends.contains(currentUserId)
        if (alreadyFriends) {
            navController.popBackStack()
            return
        }

        // Add each user to the other's friend list
        otherUserRef.update("friendList", FieldValue.arrayUnion(currentUserId))
        currentUserRef.update("friendList", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                createChatRoom(currentUserId, userId)
            }

        delay(500) // Let Firestore sync up
        UserRepository.refreshUser()
        navController.popBackStack()

    } catch (e: Exception) {
        // Log or handle error
    }
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


fun getOrCreateChatRoomId(
    currentUserId: String,
    otherUserId: String,
    onResult: (String) -> Unit
) {

    val db = FirebaseFirestore.getInstance()
    db.collection("chatRooms")
        .whereArrayContains("members", currentUserId.toString())
        .get()
        .addOnSuccessListener { snapshot ->
            val chatRoom = snapshot.documents.find { doc ->
                val members = doc.get("members") as? List<*>
                members?.contains(otherUserId) == true
            }

            if (chatRoom != null) {
                onResult(chatRoom.id)
            } else {
                val newChatRoom = hashMapOf(
                    "members" to listOf(currentUserId, otherUserId)
                )

                db.collection("chatRooms")
                    .add(newChatRoom)
                    .addOnSuccessListener { newDoc ->
                        onResult(newDoc.id)
                    }
            }
        }
}

