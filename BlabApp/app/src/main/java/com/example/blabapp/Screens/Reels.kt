package com.example.blabapp.Screens

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.google.android.gms.tasks.Tasks

// Data class for video metadata
data class VideoData(
    val id: String = "",
    val videoUrl: String = "",
    val caption: String = "",
    val userId: String = "",
    val likes: Int = 0
)



@Composable
fun ReelsScreen(navController: NavHostController) {
    var videos by remember { mutableStateOf(listOf<VideoData>()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentlyPlayingIndex by remember { mutableStateOf(0) }  // Start at the first video (index 0)

    LaunchedEffect(Unit) {
        fetchVideosFromFirebase { loadedVideos ->
            videos = loadedVideos
            isLoading = false
            Log.d("Video", videos.toString())
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // LazyColumn with vertical scrolling
        val listState = rememberLazyListState()

        // Manages scrolling and centering videos
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            items(videos) { video ->
                VideoPlayer(
                    videoUrl = video.videoUrl,
                    isPlaying = currentlyPlayingIndex == videos.indexOf(video),
                    onPlay = {
                        Log.d("Video", "Playing video: ${video.videoUrl}")
                    },
                    onPause = {
                        Log.d("Video", "Pausing video: ${video.videoUrl}")
                    }
                )
            }
        }

        // Update the currently playing video index based on scrolling
        LaunchedEffect(listState.firstVisibleItemIndex) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            if (firstVisibleIndex != currentlyPlayingIndex) {
                currentlyPlayingIndex = firstVisibleIndex
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUrl: String, isPlaying: Boolean, onPlay: () -> Unit, onPause: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    // Ensure that the ExoPlayer reacts to play/pause state
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
            onPlay() // Notify that the video is playing
        } else {
            exoPlayer.pause()
            onPause() // Notify that the video is paused
        }
    }

    // Ensures the player is properly disposed of when the screen is changed
    DisposableEffect(context) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Display the player view
    AndroidView(factory = {
        PlayerView(it).apply {
            player = exoPlayer
            useController = false // Hide controls
        }
    }, modifier = Modifier.fillMaxWidth().height(700.dp))
}




// Uploads a video to Firebase Storage
fun uploadVideoToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    val videoRef = storageRef.child("videos/${System.currentTimeMillis()}.mp4")

    videoRef.putFile(uri, storageMetadata { contentType = "video/mp4" })
        .addOnSuccessListener {
            videoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

// Saves video metadata to Firestore
fun saveVideoMetadata(videoUrl: String, caption: String, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val videoData = hashMapOf(
        "videoUrl" to videoUrl,
        "caption" to caption,
        "userId" to userId,
        "likes" to 0,
        "timestamp" to FieldValue.serverTimestamp()
    )

    db.collection("videos").add(videoData)
        .addOnSuccessListener { documentReference ->
            Log.d("Firebase", "Video metadata added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error adding video metadata: $e")
        }
}

fun fetchVideosFromFirebase(onVideosLoaded: (List<VideoData>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("videos")
        .get()
        .addOnSuccessListener { result ->
            val videos = mutableListOf<VideoData>()  // Create a mutable list to hold all video data
            val videoRequests = mutableListOf<com.google.android.gms.tasks.Task<Uri>>() // To hold all async download URL fetch tasks

            result.documents.forEach { doc ->
                val videoId = doc.id  // Use the document ID as the video file name
                val storageRef = FirebaseStorage.getInstance().reference.child("videos/$videoId.mp4")

                // Fetch the download URL asynchronously
                val downloadUrlTask = storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val videoData = VideoData(
                        id = videoId,
                        videoUrl = downloadUrl.toString(),  // Use the download URL
                        caption = doc.getString("caption") ?: "",
                        userId = doc.getString("userId") ?: "",
                        likes = doc.getLong("likes")?.toInt() ?: 0
                    )
                    videos.add(videoData)  // Add the video to the list
                }.addOnFailureListener { e ->
                    Log.e("Firebase", "Error fetching download URL: $e")
                }

                // Add this task to the list of requests
                videoRequests.add(downloadUrlTask)
            }

            // Wait for all tasks to finish before passing the videos list
            // Use Tasks.whenAll() and map it to void
            Tasks.whenAll(videoRequests).addOnCompleteListener {
                onVideosLoaded(videos)  // Now pass the full list of videos
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error fetching videos: $e")
        }
}