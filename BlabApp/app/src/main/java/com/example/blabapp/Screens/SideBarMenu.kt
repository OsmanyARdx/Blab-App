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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.blabapp.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun SidebarMenu(navController: NavController) {
    val userName = remember { mutableStateOf("Loading...") }
    val numFriends = remember { mutableStateOf(0) } // Holds the number of friends
    val profileImageUrl = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()



    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val user = UserRepository.getUser()
            user?.let {
                userName.value = it.name ?: "User"
                profileImageUrl.value = it.imageUrl ?: ""
                numFriends.value = it.friendList.size ?: 0
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

        if (profileImageUrl.value.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(profileImageUrl.value),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp).clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
        }else{
            Image(
                painter = painterResource(id = R.drawable.default_profile_photo),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp).clip(CircleShape).align(Alignment.CenterHorizontally)
            )
        }

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
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("loginScreen") {
                    popUpTo("loginScreen") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            Text("Log out", fontSize = 14.sp, color = Color.White)
        }
    }
}
