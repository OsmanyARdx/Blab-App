package com.example.blabapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.Screens.ChatScreen
import androidx.compose.ui.platform.LocalContext
import com.example.blabapp.Nav.BlabApp
import com.example.blabapp.Screens.AddFriendsScreen
import com.example.blabapp.Screens.CardMatchingGameScreen
import com.example.blabapp.Screens.FriendsListScreen
import com.example.blabapp.Screens.GameLevelScreen
import com.example.blabapp.Screens.GameSelectionScreen
import com.example.blabapp.Screens.LessonScreen
import com.example.blabapp.Screens.SplashScreen
import com.example.blabapp.Screens.StartupScreen
import com.example.blabapp.Screens.LoginScreen
import com.example.blabapp.Screens.ModuleDetailScreen
import com.example.blabapp.Screens.ModulesScreen
import com.example.blabapp.Screens.ProfileScreen
import com.example.blabapp.Screens.QuizScoreScreen
import com.example.blabapp.Screens.QuizScreen
import com.example.blabapp.Screens.ReelsScreen
import com.example.blabapp.Screens.RegisterScreen
import com.example.blabapp.Screens.WordleScreen
import com.example.blabapp.Screens.ReviewScreen
import com.example.blabapp.Screens.ScrambleScreen
import com.example.blabapp.Screens.SearchScreen
import com.example.blabapp.Screens.UploadVideoScreen
import com.example.blabapp.Screens.WordTypeGame
import com.example.blabapp.Settings.SettingsPage
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun RootScreen(accountRepository: AccountRepository) {
    val navController = rememberNavController()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    var selectedScreen by remember { mutableStateOf("home") }

    // Update selectedScreen whenever the current route changes
    LaunchedEffect(currentRoute) {
        selectedScreen = currentRoute ?: "home" // Default to home if route is null
    }

    val onBackPressedCallback = remember(currentRoute) {
        object : OnBackPressedCallback(currentRoute == "home") {
            override fun handleOnBackPressed() {
                if (currentRoute == "home") {
                    if (backPressedOnce) {
                        isEnabled = false
                        backDispatcher?.onBackPressed()
                    } else {
                        backPressedOnce = true
                        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            backPressedOnce = false
                        }, 2000)
                    }
                } else {
                    navController.popBackStack()
                }
            }
        }
    }

    DisposableEffect(backDispatcher, onBackPressedCallback) {
        backDispatcher?.addCallback(onBackPressedCallback)
        onDispose {
            onBackPressedCallback.remove()
        }
    }

    val screensWithNavBar = listOf("home", "search", "reels", "modules", "games")

    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithNavBar) {
                BottomNavigationBar(navController, selectedScreen) { route ->
                    selectedScreen = route
                    navController.navigate(route) {
                        popUpTo("home") {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = "splashScreen",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("splashScreen") { SplashScreen(navController) }
                composable("startupScreen") { StartupScreen(navController) }
                composable("loginScreen") { LoginScreen(BlabApp.accountRepository, navController) }
                composable("registerScreen") { RegisterScreen(BlabApp.accountRepository, navController) }
                composable("home") { HomeScreen("Home", navController, context = LocalContext.current) }
                composable("search") { SearchScreen(navController) }
                composable("reels") {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    ReelsScreen(navController = navController, userId = currentUserId)
                }
                composable("modules") { ModulesScreen(navController) }
                composable("moduleDetail/{moduleId}") { backStackEntry ->
                    val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                    ModuleDetailScreen(navController, moduleId)
                }
                composable("lesson/{moduleId}") { backStackEntry ->
                    val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                    LessonScreen(navController, moduleId)
                }
                composable("quiz/{moduleId}") { backStackEntry ->
                    val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                    QuizScreen(navController, moduleId)
                }
                composable("quiz_score/{score}/{totalQuestions}/{moduleId}") { backStackEntry ->
                    val score = backStackEntry.arguments?.getString("score")?.toInt() ?: 0
                    val totalQuestions = backStackEntry.arguments?.getString("totalQuestions")?.toInt() ?: 0
                    val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                    QuizScoreScreen(navController, score, totalQuestions, moduleId)
                }
                composable("games") { GameSelectionScreen(navController) }
                composable("game1"){ ScrambleScreen(navController) }
                composable("game2") { GameLevelScreen(navController) }
                composable("card_matching_game/{levelId}") { backStackEntry ->
                    val levelId = backStackEntry.arguments?.getString("levelId") ?: throw IllegalArgumentException("Level ID is required")
                    CardMatchingGameScreen(navController, levelId)
                }
                composable("messages_screen") { MessagesScreen(navController, accountRepository) }
                composable("ChatScreen/{chatRoomId}/{currentUserId}/{otherUserImage}/{currentUserImage}/{currentUserName}") { backStackEntry ->
                    val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
                    val currentUserId = backStackEntry.arguments?.getString("currentUserId") ?: ""
                    val otherUserImage = backStackEntry.arguments?.getString("otherUserImage") ?: ""
                    val currentUserImage = backStackEntry.arguments?.getString("currentUserImage") ?: ""
                    val currentUserName = backStackEntry.arguments?.getString("currentUserName") ?: ""
                    ChatScreen(navController, chatRoomId, currentUserId,currentUserImage,otherUserImage, currentUserName)
                }
                composable("friends_list") { FriendsListScreen(navController) }
                composable("add_friends") { AddFriendsScreen(navController) }
                composable("review") { ReviewScreen(navController) }
                composable("profile") { ProfileScreen(navController) }
                composable("game3") { WordTypeGame(navController) }
                composable("game4") { WordleScreen() }
                composable("settings") { SettingsPage(navController) }
                composable("upload_video_screen") { UploadVideoScreen(navController) }
            }
        }
    }
}