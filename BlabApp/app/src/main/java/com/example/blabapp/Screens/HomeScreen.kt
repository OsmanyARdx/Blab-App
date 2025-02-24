package com.example.blabapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.blabapp.Nav.AccountRepository
import com.example.blabapp.R
import com.example.blabapp.ui.theme.BlabGrey
import com.example.blabapp.ui.theme.BlabPurple
import com.example.blabapp.ui.theme.BlabYellow

@Composable
fun HomeScreen(accountRepository: AccountRepository, navController: NavController){
    val defaultProfilePic = painterResource(R.drawable.default_profile_photo)

    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(BlabYellow),
        contentAlignment = Alignment.Center
    ){

        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ){
            Row (modifier = Modifier
                .height(40.dp)){
                Text(
                    text = "Rank: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = BlabGrey
                )
            }
            Image(
                painter = defaultProfilePic,
                contentDescription = null,
                Modifier.size(100.dp)
            )
            Text(
                text = "Streak: ",
                fontSize = 20.sp
            )
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = BlabPurple, contentColor = BlabYellow),
                onClick = { navController.navigate("") }
            ) {
                Column {
                    Text(
                        text = "Phrase of the day!",
                        fontSize = 30.sp
                    )
                    //Phrase
                }
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = BlabPurple, contentColor = BlabYellow),
                onClick = { navController.navigate("") }
             ) {
                Column {
                    Text(
                        text = "Continue Module",
                        fontSize = 30.sp
                    )
                }
            }
            Row {
                //home
                //word search
                //reels
                //games

            }
        }
    }
}