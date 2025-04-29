package com.example.blabapp.Settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.blabapp.ui.theme.BlabDark
import com.example.blabapp.ui.theme.BlabDarkRed
import com.example.blabapp.ui.theme.BlabLight
import com.example.blabapp.ui.theme.BlabLightRed
import com.example.blabapp.ui.theme.BlabRed
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsPage(navHostController: NavHostController) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    selectedLanguage = document.getString("learning")
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.surface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Change Language Button
        Button(
            onClick = { showLanguageDialog = true },
            modifier = Modifier.fillMaxWidth(.7f).align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface)
        ) {
            Text("Change Language")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Change Password Button
        Button(
            onClick = { showChangePasswordDialog = true },
            modifier = Modifier.fillMaxWidth(.7f).align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface)
        ) {
            Text("Change Password")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Delete Account Button
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(.7f).align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BlabDarkRed,
                contentColor = BlabLight)
        ) {
            Text("Delete Account")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Confirm  Account Deletion
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion", color = MaterialTheme.colorScheme.background)},
            text = {
                Column {
                    Text("Enter your password to delete your account:", color = MaterialTheme.colorScheme.background)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = MaterialTheme.colorScheme.background) },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLoading = true
                        reauthenticateAndDelete(
                            password = password,
                            onSuccess = {
                                isLoading = false
                                showDeleteDialog = false
                                navHostController.navigate("startupScreen") {
                                    popUpTo(0)
                                }
                            },
                            onFailure = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    },
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Deleting..." else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            textContentColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", color = MaterialTheme.colorScheme.background)},
            text = {
                Column {
                    LanguageOption("English", "EN", selectedLanguage) { selectedLanguage = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    LanguageOption("Spanish", "ES", selectedLanguage) { selectedLanguage = it }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = auth.currentUser
                        if (user != null && selectedLanguage != null) {
                            db.collection("users").document(user.uid)
                                .update("learning", selectedLanguage)
                                .addOnSuccessListener {
                                    showLanguageDialog = false
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                    },
                    enabled = selectedLanguage != null
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            textContentColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", color = MaterialTheme.colorScheme.background) },
            text = {
                Column {
                    Text("Enter your current password:", color = MaterialTheme.colorScheme.background)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password", color = MaterialTheme.colorScheme.background) },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Enter your new password:", color = MaterialTheme.colorScheme.background)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password", color = MaterialTheme.colorScheme.background) },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = auth.currentUser
                        if (user != null && user.email != null) {
                            if (oldPassword.isBlank() || newPassword.isBlank()) {
                                errorMessage = "Please fill out both fields."
                                return@Button
                            }
                            isLoading = true
                            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            showChangePasswordDialog = false
                                            oldPassword = ""
                                            newPassword = ""
                                            errorMessage = null
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = e.message
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = e.message ?: "Reauthentication failed. Check your current password."
                                }
                        }
                    },
                    enabled = oldPassword.isNotBlank() && newPassword.isNotBlank() && !isLoading
                ) {
                    Text(if (isLoading) "Saving..." else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LanguageOption(label: String, value: String, selectedLanguage: String?, onSelect: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) }
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selectedLanguage == value,
            onClick = { onSelect(value) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

fun reauthenticateAndDelete(
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    if (user != null && user.email != null) {
        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val uid = user.uid
                    db.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                db.collection("users").document(uid)
                                    .delete()
                                    .addOnSuccessListener {
                                        user.delete()
                                            .addOnCompleteListener { deleteTask ->
                                                if (deleteTask.isSuccessful) {
                                                    onSuccess()
                                                } else {
                                                    onFailure(deleteTask.exception?.message ?: "Account deletion failed")
                                                }
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        onFailure(e.message ?: "Failed to delete Firestore user data")
                                    }
                            } else {
                                user.delete()
                                    .addOnCompleteListener { deleteTask ->
                                        if (deleteTask.isSuccessful) {
                                            onSuccess()
                                        } else {
                                            onFailure(deleteTask.exception?.message ?: "Account deletion failed")
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            onFailure(e.message ?: "Failed to access user document")
                        }
                } else {
                    onFailure(authTask.exception?.message ?: "Reauthentication failed")
                }
            }
    } else {
        onFailure("No logged-in user")
    }
}
