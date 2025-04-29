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
    val screens = listOf(
        "home" to Icons.Default.Home,
        "search" to Icons.Default.Search,
        "reels" to Icons.Default.PlayArrow,
        "modules" to Icons.Default.List,
        "games" to Icons.Default.Face
    )

    NavigationBar(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary).height(70.dp),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        screens.forEach { (screen, icon) ->
            NavigationBarItem(
                selected = screen == selectedScreen,
                onClick = {
                    navController.navigate(screen)
                    onScreenSelected(screen)
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