package com.example.blabapp.Screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.blabapp.ui.theme.BlabDarkRed
import com.example.blabapp.ui.theme.BlabRed
import com.google.android.gms.tasks.Task
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
fun InteractionBar(
    videoId: String,
    videoUrl: String,
    userId: String,
    onCommentClick: () -> Unit
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(0) }

    LaunchedEffect(videoId) {
        val db = FirebaseFirestore.getInstance()
        val videoRef = db.collection("videos").document(videoId)

        videoRef.get().addOnSuccessListener { doc ->
            likeCount = doc.getLong("likes")?.toInt() ?: 0
        }

        db.collection("videos").document(videoId)
            .collection("likes").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                isLiked = snapshot.exists()
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like Icon + Count side by side
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val db = FirebaseFirestore.getInstance()
                val videoRef = db.collection("videos").document(videoId)
                val likeRef = videoRef.collection("likes").document(userId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(likeRef)
                    val isCurrentlyLiked = snapshot.exists()

                    if (!isCurrentlyLiked) {
                        // LIKE
                        transaction.set(likeRef, mapOf("liked" to true))
                        transaction.update(videoRef, "likes", FieldValue.increment(1))
                        isLiked = true
                        likeCount += 1
                    } else {
                        // UNLIKE
                        transaction.delete(likeRef)
                        transaction.update(videoRef, "likes", FieldValue.increment(-1))
                        isLiked = false
                        likeCount -= 1
                    }
                }

            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) BlabRed else Color.Gray
                )
            }
            Text(
                text = "$likeCount",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Comment Button
        IconButton(onClick = onCommentClick) {
            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment")
        }

        // Share Button
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Watch this video: $videoUrl")
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }) {
            Icon(Icons.Default.Share, contentDescription = "Share")
        }
    }
}


@Composable
fun ReelsScreen(navController: NavHostController, userId: String) {
    var videos by remember { mutableStateOf(listOf<VideoData>()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentlyPlayingIndex by remember { mutableStateOf(0) } // Start at the first video (index 0)
    var expandedComments by remember { mutableStateOf<String?>(null) }  // Track the expanded comment section for a single video

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
                Column {
                    VideoPlayer(
                        videoData = video,
                        isPlaying = currentlyPlayingIndex == videos.indexOf(video),
                        onPlay = {
                            Log.d("Video", "Playing video: ${video.videoUrl}")
                        },
                        onPause = {
                            Log.d("Video", "Pausing video: ${video.videoUrl}")
                        },
                        userId = userId,
                        onCommentClick = {
                            // Toggle comment section for the current video
                            expandedComments = if (expandedComments == video.id) null else video.id
                        },
                        expandedComments = expandedComments,
                        setExpandedComments = { expandedComments = it }
                    )
                }
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
fun VideoPlayer(
    videoData: VideoData,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    userId: String,
    onCommentClick: () -> Unit,
    expandedComments: String?,
    setExpandedComments: (String?) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoData.videoUrl))
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

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(725.dp)
        ) {

            // Display the player view
            AndroidView(factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false // Hide controls
                }
            }, modifier = Modifier.fillMaxSize())

            // Comment section overlays bottom half of video
            AnimatedVisibility(
                visible = expandedComments == videoData.id,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CommentSection(
                        videoId = videoData.id,
                        userId = userId,
                        onBack = { setExpandedComments(null) },
                        onSubmitComment = { commentText ->
                            postCommentToFirestore(
                                videoId = videoData.id,
                                userId = userId,
                                commentText = commentText,
                                onSuccess = {},
                                onFailure = {}
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    // Interaction bar below video
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        InteractionBar(
            videoId = videoData.id,
            videoUrl = videoData.videoUrl,
            userId = userId,
            onCommentClick = {
                if (expandedComments == videoData.id) {
                    setExpandedComments(null)
                } else {
                    setExpandedComments(videoData.id)
                }
            }
        )
    }

}

// Uploads a video to Firebase Storage
fun uploadVideoToFirebase(
    uri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
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

// Fetch videos from Firestore
fun fetchVideosFromFirebase(onVideosLoaded: (List<VideoData>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("videos")
        .get()
        .addOnSuccessListener { result ->
            val videos = mutableListOf<VideoData>()
            val tasks = mutableListOf<Task<Uri>>()

            for (doc in result.documents) {
                val videoId = doc.id
                val caption = doc.getString("caption") ?: ""
                val userId = doc.getString("userId") ?: ""
                val likes = doc.getLong("likes")?.toInt() ?: 0
                val storageRef = FirebaseStorage.getInstance().reference.child("videos/$videoId.mp4")

                val downloadTask = storageRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        videos.add(
                            VideoData(
                                id = videoId,
                                videoUrl = downloadUrl.toString(),
                                caption = caption,
                                userId = userId,
                                likes = likes
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Error getting download URL: $e")
                    }

                tasks.add(downloadTask)
            }

            Tasks.whenAllComplete(tasks).addOnSuccessListener {
                onVideosLoaded(videos)
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error getting video documents: $e")
        }
}
