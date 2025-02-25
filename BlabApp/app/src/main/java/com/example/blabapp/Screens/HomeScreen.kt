package com.example.blabapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun HomeScreen(title: String, navController: NavHostController, profileImageUrl: String) {

    val userStreak = remember { mutableStateOf("Loading...") }
    val userRank = remember { mutableStateOf("Loading...") }
    val userName = remember { mutableStateOf("Loading...") }

    // Get user data from Firebase
    LaunchedEffect(Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userStreak.value = document.getString("userStreak") ?: "0"
                        userRank.value = document.getString("rank") ?: "Rookie"
                        userName.value = document.getString("name") ?: "user"
                    }
                }
                .addOnFailureListener {
                    userStreak.value = "Error"
                    userRank.value = "Error"
                }
        } else {
            userStreak.value = "Not Logged In"
            userRank.value = "Not Available"
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(BlabYellow)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Messenger",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable { navController.navigate("messages") }
        )

        Text(
            text = userRank.value.toString(),
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Text(
                text = "Streak: " + userStreak.value,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.pfp), //replace with actual user's image
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .border(1.dp, BlabPurple, CircleShape)
                    .background(BlabPurple)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userName.value.toString(),
                fontSize = 34.sp,
                color = BlabPurple,
                fontWeight = FontWeight.Bold
            )


            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(3.dp, BlabPurple, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { /* TODO: Handle button click */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column {
                        Text(
                            text = "Phrase of the Day:",
                            color = BlabPurple,
                            fontSize = 18.sp
                        )
                    }
                }

            }
        }
    }
}




