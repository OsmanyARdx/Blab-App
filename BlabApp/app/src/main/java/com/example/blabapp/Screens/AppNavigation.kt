
package com.example.blabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = Modifier.height(80.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        screens.forEach { (screen, icon) ->
            NavigationBarItem(
                selected = screen == selectedScreen,
                onClick = {


                    navController.navigate(screen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    onScreenSelected(screen)
                },
                icon = {
                    Box(modifier = Modifier.size(32.dp)) {
                        Icon(
                            icon,
                            contentDescription = screen,
                            tint = if (screen == selectedScreen) MaterialTheme.colorScheme.onTertiary else Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun ScreenContent(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Yellow)
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}
