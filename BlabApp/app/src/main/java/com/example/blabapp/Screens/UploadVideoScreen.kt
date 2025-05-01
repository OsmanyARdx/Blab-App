package com.example.blabapp.Screens

import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.ktx.storageMetadata
import com.google.firebase.storage.ktx.storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVideoScreen(navController: NavHostController) {
    val context = LocalContext.current
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf(TextFieldValue("")) }
    var isUploading by remember { mutableStateOf(false) }

    // Video picker launcher
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            videoUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.surface // Using surface for icons
                )
            }
            Text(
                text = "Upload",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.surface // Using surface for title
            )
            Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Video Selection and Preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = { pickVideoLauncher.launch("video/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(50.dp), // Rounded shape like your login button
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary // Primary color for text
                ),
                border = androidx.compose.foundation.BorderStroke(
                    3.dp,
                    MaterialTheme.colorScheme.primary
                ), // Corrected border
            ) {
                Text(
                    text = if (videoUri == null) "Select Video" else "Change Video",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary // Primary color for text
                )
            }

            // Video Preview
            videoUri?.let { selectedUri ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            VideoView(context).apply {
                                setMediaController(null)
                            }
                        },
                        update = { videoView ->
                            videoView.setVideoURI(selectedUri)
                            videoView.setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.setVolume(0f, 0f)
                                mp.start()
                            }
                            videoView.setOnCompletionListener { mp ->
                                mp.start()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Caption Input
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = {
                Text(
                    text = "Caption",
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) // Using surface for label
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(8.dp)
                ), // Background color
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary, // Primary color for focused border
                unfocusedBorderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), // Surface for unfocused border
                cursorColor = MaterialTheme.colorScheme.secondary, // Secondary color for cursor
                focusedLabelColor = MaterialTheme.colorScheme.primary, // Primary for focused label
                unfocusedLabelColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) // Surface for unfocused label
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.weight(1f))

// Centered Upload Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    videoUri?.let { uri ->
                        isUploading = true
                        uploadVideoToFirebase(uri, caption.text, {
                            isUploading = false
                            navController.popBackStack()
                            Toast.makeText(context, "Video uploaded!", Toast.LENGTH_SHORT).show()
                        }, { exception ->
                            isUploading = false
                            Toast.makeText(
                                context,
                                "Upload failed: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                },
                modifier = Modifier
                    .border(3.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp)),
                enabled = videoUri != null,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = if (isUploading) "Uploading..." else "Post Video",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface
                )
                if (isUploading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun uploadVideoToFirebase(uri: Uri, caption: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val videoId = System.currentTimeMillis().toString()
    val storageRef = FirebaseStorage.getInstance().reference.child("videos/$videoId.mp4")

    storageRef.putFile(uri, storageMetadata { contentType = "video/mp4" })
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
                saveVideoMetadata(downloadUrl.toString(), caption, videoId, userId)
                onSuccess()
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

fun saveVideoMetadata(videoUrl: String, caption: String, videoId: String, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val videoData = hashMapOf(
        "videoUrl" to videoUrl,
        "caption" to caption,
        "likes" to 0,
        "timestamp" to FieldValue.serverTimestamp(),
        "userId" to userId,
        "id" to videoId
    )

    db.collection("videos").document(videoId).set(videoData)
        .addOnSuccessListener { /* Success */ }
        .addOnFailureListener { /* Failure */ }
}