package com.example.blabapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.blabapp.ui.theme.BlabAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlabAppTheme {
                // NavController setup
                val navController = rememberNavController()

                RegistrationScreen(navController)
            }
        }
    }
}


    private fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email or password cannot be empty")
            return
        }


        //to work when firebase get added
        /*
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("Authentication", "User authenticated successfully")
                    onResult(true, "Login successful")
                } else {
                    val error = task.exception?.message ?: "Unknown error occurred"
                    Log.e("Authentication", "Error: $error")
                    onResult(false, error)
                }
            }
           */

//    }

    fun registerUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email or password cannot be empty")
            return
        }

        //to work when firebase get added
        /*
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("Registration", "User registered successfully")
                    onResult(true, "Registration successful")
                } else {
                    val error = task.exception?.message ?: "Unknown error occurred"
                    Log.e("Registration", "Error: $error")
                    onResult(false, error)
                }
            }
        */
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BlabAppTheme {
        Greeting("Android")
    }
}