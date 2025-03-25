package com.example.blabapp.Screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.Design.InputField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ProfileScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var showConfirmButton by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            showConfirmButton = true
        } else {
            Toast.makeText(context, "Image selection canceled", Toast.LENGTH_SHORT).show()
        }
    }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var bio by remember { mutableStateOf("") }
    var images by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                bio = document.getString("userBio") ?: ""
                images = document.getString("imageUrl") ?: ""

                val nameParts = name.split(" ")
                if (nameParts.size >= 2) {
                    firstName = nameParts[0]
                    lastName = nameParts[1]
                } else {
                    firstName = nameParts[0]
                    lastName = ""
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isEditingProfile) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Text(text = bio, modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onTertiary)

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text(text = "Edit Image", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showConfirmButton) {
                Button(
                    onClick = {
                        currentUser?.uid?.let { uid ->
                            val fileName = UUID.randomUUID().toString()
                            val imageRef = storage.reference.child("profile_images/$fileName")
                            imageRef.putFile(selectedImageUri!!).addOnSuccessListener {
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val updates = mapOf("imageUrl" to uri.toString())
                                    db.collection("users").document(uid).update(updates)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Image updated", Toast.LENGTH_SHORT).show()
                                            showConfirmButton = false
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to update image", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }.addOnFailureListener {
                                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(220.dp)
                        .padding(16.dp)
                ) {
                    Text(text = "Confirm Image")
                }
            }

            Button(onClick = { isEditingProfile = true }) {
                Text(text = "Edit Profile", fontSize = 15.sp)
            }

        } else {
            InputField(label = "First Name", value = firstName, onValueChange = { firstName = it })
            Spacer(modifier = Modifier.height(8.dp))
            InputField(label = "Last Name", value = lastName, onValueChange = { lastName = it })
            Spacer(modifier = Modifier.height(8.dp))
            InputField(label = "Email", value = email, onValueChange = { email = it })
            Spacer(modifier = Modifier.height(8.dp))
            InputField(label = "Bio", value = bio, onValueChange = { bio = it })
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                currentUser?.uid?.let { uid ->
                    val fullName = "$firstName $lastName"
                    val updates = hashMapOf(
                        "name" to fullName,
                        "userBio" to bio,
                        "email" to email
                    )
                    db.collection("users").document(uid).set(updates)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                            isEditingProfile = false
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                }
            }) {
                Text(text = "Confirm Changes", fontSize = 15.sp)
            }
        }
    }
}
