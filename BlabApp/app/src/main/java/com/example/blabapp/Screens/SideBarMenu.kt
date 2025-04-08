package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SidebarMenu(navController: NavController) {
    // UI states for user data
    val userName = remember { mutableStateOf("Loading...") }
    val numFriends = remember { mutableStateOf(0) }
    val profileImageUrl = remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Realtime Firestore listener for user profile
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userName.value = snapshot.getString("name") ?: "User"
                        profileImageUrl.value = snapshot.getString("imageUrl") ?: ""
                        val friends = snapshot.get("friendList") as? List<*> ?: emptyList<Any>()
                        numFriends.value = friends.size
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.7f)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(150.dp))

        // Profile Image
        if (profileImageUrl.value.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(profileImageUrl.value),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.default_profile_photo),
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Username
        Text(
            userName.value,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Friend Count
        Text(
            "${numFriends.value} Friends",
            fontSize = 17.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sidebar Menu Buttons
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(listOf("Profile", "Friend List", "Settings", "Saved")) { item ->
                Button(
                    onClick = {
                        when (item) {
                            "Profile" -> navController.navigate("profile")
                            "Friend List" -> navController.navigate("friends_list")
                            "Settings" -> {} // Fill this later
                            "Saved" -> {} // Fill this later
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(item, fontSize = 20.sp, color = Color.White)
                }
            }
        }

        // Logout Button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("loginScreen") {
                    popUpTo("loginScreen") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text("Log out", fontSize = 14.sp, color = Color.White)
        }
    }
}
