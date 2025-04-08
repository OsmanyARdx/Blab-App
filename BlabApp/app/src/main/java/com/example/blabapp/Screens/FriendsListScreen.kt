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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun FriendsListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val friendsList = remember { mutableStateOf<List<String>>(emptyList()) }
    val friendNames = remember { mutableStateOf<List<String>>(emptyList()) } // Holds the friend names
    val filteredFriends = friendNames.value.filter { it.contains(searchQuery, ignoreCase = true) }
    val isSidebarVisible = remember { mutableStateOf(false) }
    val profileImageUrl = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Fetch user profile info (like profile image URL)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val user = UserRepository.getUser()
            user?.let {
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
                                    .border(1.dp, BlabPurple, CircleShape)
                                    .background(BlabPurple),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile_photo),
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .border(1.dp, BlabPurple, CircleShape)
                                    .background(BlabPurple)
                            )
                        }
                    }

                    Text(
                        text = "Friends List",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.align(Alignment.Center).padding(top = 16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(filteredFriends) { friend ->
                                Text(
                                    text = friend,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
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
                                modifier = Modifier.width(200.dp).align(Alignment.Center)
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black.copy(alpha = 0.5f), RoundedCornerShape(0.dp))
                    .clickable { isSidebarVisible.value = false }
            ) {
                SidebarMenu(navController)
            }
        }
    }
}


