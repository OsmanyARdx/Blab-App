package com.example.blabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.blabapp.ui.theme.BlabYellow

@Composable
fun MessagesScreen(navController: NavHostController) {
    val messages = listOf("Message 1", "Message 2", "Message 3", "Message 4")
    val pagerState = rememberPagerState(pageCount = { messages.size })

    Box(modifier = Modifier.fillMaxSize().background(BlabYellow)) {
        // Back Arrow Icon inside Box with correct alignment
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clickable { navController.popBackStack() }
        )

    }
}
