package com.example.blabapp.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.blabapp.Design.InputField
import com.example.blabapp.R
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.ui.theme.BlabPurple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ProfileScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val coroutineScope = rememberCoroutineScope()

    var isEditingProfile by remember { mutableStateOf(false) }
    var showEditImageFields by remember { mutableStateOf(false) }

    val userName = remember { mutableStateOf("Loading...") }
    val profileImageUrl = remember { mutableStateOf("") }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var bio by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    // Load user info
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val user = UserRepository.getUser()
            user?.let {
                userName.value = it.name ?: "User"
                profileImageUrl.value = it.imageUrl ?: ""
            }
        }
    }

    // Fetch Firestore data
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                bio = document.getString("userBio") ?: ""
                profileImageUrl.value = document.getString("imageUrl") ?: ""

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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isEditingProfile) {
            // Display profile image
            val imageToShow = previewImageUrl ?: profileImageUrl.value

            if (imageToShow.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageToShow),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_profile_photo),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bio,
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showEditImageFields = !showEditImageFields
                    previewImageUrl = null
                    newImageUrl = ""
                },
                modifier = Modifier.fillMaxWidth(.7f),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(text = if (showEditImageFields) "Cancel" else "Edit Image", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showEditImageFields) {
                InputField(
                    label = "Paste Image URL",
                    value = newImageUrl,
                    onValueChange = {
                        newImageUrl = it
                        previewImageUrl = if (it.isNotBlank()) it else null
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        currentUser?.uid?.let { uid ->
                            val updates = mapOf("imageUrl" to newImageUrl)
                            db.collection("users").document(uid).update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Image URL updated", Toast.LENGTH_SHORT).show()
                                    profileImageUrl.value = newImageUrl
                                    previewImageUrl = null
                                    newImageUrl = ""
                                    showEditImageFields = false
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update image", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    enabled = newImageUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(.7f),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.surface)
                ) {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Confirm Image URL")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { isEditingProfile = true },
                modifier = Modifier.fillMaxWidth(.7f),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(text = "Edit Profile", fontSize = 15.sp)
            }

        } else {
            // Editable Profile Form
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
                    val updates = mapOf(
                        "name" to fullName,
                        "userBio" to bio,
                        "email" to email
                    )
                    db.collection("users").document(uid).update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                            isEditingProfile = false
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                }
            },
                modifier = Modifier.fillMaxWidth(),shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(text = "Confirm Changes", fontSize = 15.sp)
            }
        }
    }
}
