package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddFriendsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = remember { mutableStateOf<List<User>>(emptyList()) }
    val isSidebarVisible = remember { mutableStateOf(false) }
    val profileImageUrl = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(searchQuery) {
        coroutineScope.launch {
            val user = UserRepository.getUser()
            user?.let {
                profileImageUrl.value = it.imageUrl ?: ""
            }
        }


        // Only query Firestore if the search query is not empty
        if (searchQuery.isNotEmpty()) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users")
                .whereGreaterThanOrEqualTo("name", searchQuery)
                .whereLessThanOrEqualTo("name", searchQuery + "\uf8ff")
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
                .addOnFailureListener { e -> }
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
                    text = "Add Friends",
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
                        items(filteredUsers.value) { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(user.name, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                                Button(onClick = { addFriend(user.userId) }) {
                                    Text("Add")
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
                            modifier = Modifier.width(250.dp).align(Alignment.Center)
                        ) {
                            Text("Back to Friends", fontSize = 25.sp)
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

fun addFriend(userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUserId = firebaseAuth.currentUser?.uid

    if (currentUserId != null) {
        // Update the current user's friends list with userId
        firestore.collection("users").document(currentUserId)
            .update("friendList", FieldValue.arrayUnion(userId)) // Add userId to friends list
            .addOnSuccessListener {
                val coroutineScope = CoroutineScope(Dispatchers.IO)
                coroutineScope.launch {
                    refreshUser()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure (e.g., show an error message)
            }
    }
}

data class User(val name: String, val userId: String)


