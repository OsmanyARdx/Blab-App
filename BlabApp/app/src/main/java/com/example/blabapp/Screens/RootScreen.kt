package com.example.blabapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import com.example.blabapp.MessagesScreen
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
import com.example.blabapp.Screens.WordTypeGame

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun RootScreen(accountRepository: AccountRepository) {
    val navController = rememberNavController()


    val screensWithNavBar = listOf("home", "search", "reels", "modules", "games", "friends_list", "add_friends", "lesson/{moduleId}", "moduleDetail/{moduleId}", "quiz/{moduleId}", "quiz_score/{score}/{totalQuestions}", "games", "profile")


    var selectedScreen by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute in screensWithNavBar) {
                BottomNavigationBar(navController, selectedScreen) { route ->
                    selectedScreen = route
                    navController.navigate(route) {
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
                // Handle all your screens here in a single NavHost
                composable("splashScreen") { SplashScreen(navController) }
                composable("startupScreen") { StartupScreen(navController) }
                composable("loginScreen") { LoginScreen(BlabApp.accountRepository, navController) }
                composable("registerScreen") { RegisterScreen(BlabApp.accountRepository, navController) }
                composable("home") { HomeScreen("Home", navController, context = LocalContext.current) }
                composable("search") { ScreenContent("Search") }
                composable("reels") { ReelsScreen(navController) }
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

            }
        }
    }
}
