package com.example.blabapp

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Creates NavBar
 * It clears AddSaleScreen when you navigate
 * to SalesListsScreen
 */


@Composable
fun RootScreen(){
    Text("Root Screen", fontSize=30.sp)
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    val navItemsList = listOf(
        MyNavItem(title="Home",
            iconSelected = Icons.Filled.Home,
            iconUnselected = Icons.Outlined.Home,
            route="LoginScreen"
        )
    )
    val context = LocalContext.current
    val navController = rememberNavController()
    var isVisible by rememberSaveable { mutableStateOf(false) }
    Scaffold (
        bottomBar = {if(isVisible){
            NavigationBar {
                navItemsList.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = (selectedItemIndex == index),
                        onClick = {
                            selectedItemIndex = index
                            Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                            if(item.route=="LoginScreen"){
                                navController.navigate(item.route)
                            }
                            else{
                                navController.popBackStack()
                            }

                        },
                        label = { Text(text = item.title) },
                        icon = {
                            Icon(
                                contentDescription = item.title,
                                imageVector =
                                if (index == selectedItemIndex) item.iconSelected
                                else item.iconUnselected
                            )
                        }
                    )
                }
            }
        }
        }
    )
    { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            NavHost(navController=navController, startDestination="LoginScreen"){

                composable(route="LoginScreen"){
                    isVisible=false
                    LoginScreen(BlabApp.accountRepository, navController)
                }
            }
        }
    }
}