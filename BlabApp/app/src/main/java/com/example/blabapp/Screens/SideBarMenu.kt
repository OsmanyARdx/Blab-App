package com.example.blabapp.Screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.R
import com.example.blabapp.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SidebarMenu(
    navController: NavController,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val userName = remember { mutableStateOf("Loading...") }
    val numFriends = remember { mutableStateOf(0) }
    val profileImageUrl = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(-700f) }
    val shouldRenderSidebar = remember { mutableStateOf(false) }

    // Detect when the visibility changes to trigger the animation
    LaunchedEffect(isVisible) {
        if (isVisible) {
            shouldRenderSidebar.value = true
            // Slide the sidebar in
            offsetX.animateTo(0f, animationSpec = tween(durationMillis = 300))
        } else {
            // Slide the sidebar out
            offsetX.animateTo(-700f, animationSpec = tween(durationMillis = 300))
            delay(300) // Wait for the animation to complete
            shouldRenderSidebar.value = false // Hide sidebar completely after the animation finishes
        }
    }

    // Ensure that the sidebar is only rendered when it should be visible
    if (shouldRenderSidebar.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background overlay to dismiss the menu
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        onClick = { onDismiss() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )

            // Sidebar panel
            Column(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.toInt(), 0) }
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(16.dp)
            ) {
                // Fetch user data
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

                // Profile image
                if (profileImageUrl.value.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl.value),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .border(2.dp, color = MaterialTheme.colorScheme.secondary, CircleShape)
                            .size(120.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile_photo),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .border(2.dp, color = MaterialTheme.colorScheme.secondary, CircleShape)
                            .size(120.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display username
                Text(
                    userName.value,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display number of friends
                Text(
                    "${numFriends.value} Friends",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button list
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 16.dp)
                ) {
                    items(listOf("Profile", "Friend List", "Settings", "Log out")) { item ->
                        Button(
                            onClick = {
                                when (item) {
                                    "Profile" -> navController.navigate("profile")
                                    "Friend List" -> navController.navigate("friends_list")
                                    "Settings" -> navController.navigate("settings")
                                    "Log out" -> {
                                        FirebaseAuth.getInstance().signOut()
                                        navController.navigate("loginScreen") {
                                            popUpTo("loginScreen") { inclusive = true }
                                        }
                                    }
                                }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(item, fontSize = 20.sp, color = MaterialTheme.colorScheme.surface)
                        }
                    }
                }
            }
        }
    }
}
