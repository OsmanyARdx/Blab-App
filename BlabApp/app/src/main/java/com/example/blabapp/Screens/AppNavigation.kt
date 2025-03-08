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
import com.example.blabapp.Screens.MessagesScreen
import com.example.blabapp.Screens.ModulesScreen
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow

@Composable
fun AppNavigation(accountRepository: AccountRepository, navController: NavController) {
    // Initialize NavController here
    val navController = rememberNavController()

    var selectedScreen by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, selectedScreen) { selectedScreen = it }
        },
        containerColor = BlabYellow
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Define NavHost here with correct order
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen("Home", navController, profileImageUrl = "", context = LocalContext.current) }
                composable("search") { ScreenContent("Search") }
                composable("reels") { ScreenContent("Reels") }
                composable("modules") { ModulesScreen(navController) }
                composable("learning/{moduleId}") { backStackEntry ->
                    val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                    LessonScreen(navController, moduleId)
                }
                composable("games") { ScreenContent("Games") }
                composable("messages") { MessagesScreen(navController) }
            }
        }
    }
}


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    selectedScreen: String,
    onScreenSelected: (String) -> Unit
) {
    val screens = listOf(
        "home" to Icons.Default.Home,
        "search" to Icons.Default.Search,
        "reels" to Icons.Default.PlayArrow,
        "modules" to Icons.Default.List,
        "games" to Icons.Default.Face
    )

    NavigationBar(
        modifier = Modifier.background(BlabYellow),
        containerColor = BlabYellow
    ) {
        screens.forEach { (screen, icon) ->
            NavigationBarItem(
                selected = screen == selectedScreen,
                onClick = {
                    // Handle navigation logic
                    navController.navigate(screen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    onScreenSelected(screen)
                },
                icon = {
                    Icon(
                        icon,
                        contentDescription = screen,
                        tint = if (screen == selectedScreen) BlabPurple else Color.Black
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}

@Composable
fun ScreenContent(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(BlabYellow)
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
