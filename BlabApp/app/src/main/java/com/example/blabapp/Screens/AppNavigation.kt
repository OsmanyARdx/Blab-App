package com.example.blabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Screens.LessonScreen
import com.example.blabapp.MessagesScreen
import com.example.blabapp.Screens.ModulesScreen


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    selectedScreen: String,
    onScreenSelected: (String) -> Unit
) {
    val mainScreens = listOf("home", "search", "reels", "modules", "games")
    val screens = mainScreens.map { it to when (it) {
        "home" -> Icons.Default.Home
        "search" -> Icons.Default.Search
        "reels" -> Icons.Default.PlayArrow
        "modules" -> Icons.Default.List
        "games" -> Icons.Default.Face
        else -> Icons.Default.QuestionMark // Fallback
    }}

    NavigationBar(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary).height(70.dp),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        screens.forEach { (screen, icon) ->
            NavigationBarItem(
                selected = screen == selectedScreen,
                onClick = {
                    onScreenSelected(screen) // Update the selected screen state

                    if (mainScreens.contains(screen)) {
                        navController.navigate(screen) {
                            // Pop back to the "home" screen and clear everything above it
                            popUpTo("home") {
                                inclusive = false // Don't remove "home"
                                saveState = true     // Optionally save state of "home"
                            }
                            launchSingleTop = true // Ensure only one instance of the screen
                            restoreState = true    // Optionally restore state
                        }
                    }
                },
                icon = {
                    Icon(
                        icon,
                        contentDescription = screen,
                        modifier = Modifier.size(24.dp),
                        tint = if (screen == selectedScreen) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
fun ScreenContent(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}