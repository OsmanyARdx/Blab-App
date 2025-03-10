package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.blabapp.Nav.Lesson
import com.example.blabapp.Nav.Module
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ModulesScreen(navController: NavHostController) {
    val modules = getModule()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select a Module", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))

        if (modules.value.isEmpty()) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(modules.value) { module ->
                    ModuleItem(module) {
                        navController.navigate("learning/${module.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleItem(module: Module, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Module ${module.moduleNum}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(text = "Topic: ${module.topic}", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
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
                // Map documents to Module objects
                val moduleList = documents.mapNotNull { document ->
                    val moduleNum = document.getLong("moduleNum")?.toInt()
                    val topic = document.getString("topic")
                    if (moduleNum != null && topic != null) {
                        Module(moduleNum, topic, document.id)  // Include moduleId
                    } else null
                }

                // Sort modules by moduleNum
                val sortedModules = moduleList.sortedBy { it.moduleNum }

                // Update state with sorted modules
                modules.value = sortedModules
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching modules: ${exception.message}")
            }
    }

    return modules
}
