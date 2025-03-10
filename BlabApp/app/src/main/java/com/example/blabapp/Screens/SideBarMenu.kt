package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.blabapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SidebarMenu(navController: NavController) {
    val userName = remember { mutableStateOf("Loading...") }
    val numFriends = remember { mutableStateOf(0) } // Holds the number of friends

    LaunchedEffect(Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Safely retrieve the user's name
                        userName.value = document.getString("name") ?: "User"
                        // Retrieve the friends list and count the number of friends
                        val friends = document.get("friendList") as? List<String>
                        numFriends.value = friends?.size ?: 0 // Update friend count
                    } else {
                        userName.value = "No User Data Found"
                    }
                }
                .addOnFailureListener {
                    userName.value = "Failed to Load User Data"
                }
        } else {
            userName.value = "Not Logged In"
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

        Image(
            painter = painterResource(id = R.drawable.pfp),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(120.dp).clip(CircleShape).align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(userName.value, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(
            Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(8.dp))

        // Show the number of friends
        Text("${numFriends.value} Friends", fontSize = 17.sp, color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(listOf("Profile", "Friend List", "Settings", "Saved")) { item ->
                Button(
                    onClick = {
                        when (item) {
                            "Profile" -> {}
                            "Friend List" -> navController.navigate("friends_list")
                            "Settings" -> {}
                            "Saved" -> {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(item, fontSize = 20.sp, color = Color.White)
                }
            }
        }

        Button(
            onClick = {} ,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            Text("Log out", fontSize = 14.sp, color = Color.White)
        }
    }
}
