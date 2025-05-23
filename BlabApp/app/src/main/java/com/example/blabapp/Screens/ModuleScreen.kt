package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blabapp.Nav.Module
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ModulesScreen(navController: NavHostController) {
    val modules = getModule()
    val completeMod = remember { mutableStateOf<List<String>>(emptyList()) }
    val userLearningPreference = remember { mutableStateOf("ES") }

    // Fetch completed modules from Firestore when the screen is navigated to
    LaunchedEffect(Unit) {

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val completedModules = document.get("completeMod") as? List<String> ?: emptyList()
                    completeMod.value = completedModules
                    val learningPreference = document.getString("learning") ?: "ES"
                    userLearningPreference.value = learningPreference
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error fetching completed modules: ${exception.message}")
                }
    }



    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a Module",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (modules.value.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(modules.value) { module ->
                        val isCompleted = module.id in completeMod.value
                        ModuleItem(
                            navController = navController,
                            module = module,
                            userLearningPreference = userLearningPreference.value,
                            isCompleted = isCompleted
                        ) {
                            navController.navigate("moduleDetail/${module.id}")
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                navController.navigate("review")
            },
            modifier = Modifier.fillMaxWidth(.7f).align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.background)
        ) {
            Text(text = "Review Items", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}


@Composable
fun ModuleItem(
    navController: NavHostController,
    module: Module,
    userLearningPreference: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.tertiary
    val displayTopic = if (userLearningPreference == "ES") module.topic else module.topicES

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Module ${module.moduleNum}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
            Text(
                text = "Topic: $displayTopic",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}


@Composable
fun getModule(): MutableState<List<Module>> {
    val modules = remember { mutableStateOf<List<Module>>(emptyList()) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("modules")
            .get()
            .addOnSuccessListener { documents ->
                val moduleList = documents.mapNotNull { document ->
                    val moduleNum = document.getLong("moduleNum")?.toInt()
                    val topic = document.getString("topic")
                    val topicES = document.getString("topicES")
                    if (moduleNum != null && topic != null) {
                        Module(moduleNum, topic, document.id, topicES.toString())
                    } else null
                }

                Log.d("Mods", moduleList.toString())
                val sortedModules = moduleList.sortedBy { it.moduleNum }
                modules.value = sortedModules
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching modules: ${exception.message}")
            }
    }

    return modules
}

