package com.example.blabapp.Screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.blabapp.ui.theme.BlabDarkRed
import com.example.blabapp.ui.theme.BlabRed
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Data class for video metadata (without the direct video URL initially)
data class VideoMetadata(
    val id: String = "",
    val caption: String = "",
    val userId: String = "",
    val likes: Int = 0
)

// Data class for video information including the URL
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
            try {
                context.startActivity(Intent.createChooser(intent, "Share via"))
            } catch (e: Exception) {
                Log.e("ShareIntent", "Error launching share intent: ${e.localizedMessage}")
                Toast.makeText(context, "Could not share video.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Icon(Icons.Default.Share, contentDescription = "Share")
        }
    }
}

@Composable
fun ReelsScreen(navController: NavHostController, userId: String) {
    var videoMetadataList by remember { mutableStateOf(listOf<VideoMetadata>()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentlyPlayingIndex by remember { mutableStateOf(0) }
    var expandedComments by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fetchVideoMetadataFromFirebase { loadedMetadata ->
            videoMetadataList = loadedMetadata
            isLoading = false
            Log.d("VideoMetadata", videoMetadataList.toString())
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
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            items(videoMetadataList) { videoMetadata ->
                val isCurrentItemVisible = remember {
                    derivedStateOf {
                        listState.layoutInfo.visibleItemsInfo.any { it.index == videoMetadataList.indexOf(videoMetadata) }
                    }
                }
                var videoUri by remember { mutableStateOf<Uri?>(null) }
                var isVideoLoading by remember { mutableStateOf(false) }

                LaunchedEffect(isCurrentItemVisible.value) {
                    if (isCurrentItemVisible.value && videoUri == null) {
                        isVideoLoading = true
                        fetchVideoUrl(videoId = videoMetadata.id) { uri ->
                            videoUri = uri
                            isVideoLoading = false
                        }
                    }
                }

                Column {
                    if (videoUri != null) {
                        VideoPlayer(
                            videoData = VideoData(
                                id = videoMetadata.id,
                                videoUrl = videoUri.toString(),
                                caption = videoMetadata.caption,
                                userId = videoMetadata.userId,
                                likes = videoMetadata.likes
                            ),
                            isPlaying = currentlyPlayingIndex == videoMetadataList.indexOf(videoMetadata),
                            onPlay = {
                                Log.d("Video", "Playing video: ${videoMetadata.id}")
                            },
                            onPause = {
                                Log.d("Video", "Pausing video: ${videoMetadata.id}")
                            },
                            userId = userId,
                            onCommentClick = {
                                expandedComments = if (expandedComments == videoMetadata.id) null else videoMetadata.id
                            },
                            expandedComments = expandedComments,
                            setExpandedComments = { expandedComments = it }
                        )
                    } else {
                        // Placeholder while the video is loading or not yet visible
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(725.dp)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVideoLoading) {
                                CircularProgressIndicator(color = Color.White)
                            } else {
                                Text("Loading Preview", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(listState.firstVisibleItemIndex) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            if (firstVisibleIndex != currentlyPlayingIndex && firstVisibleIndex < videoMetadataList.size) {
                currentlyPlayingIndex = firstVisibleIndex
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = {
                    navController.navigate("upload_video_screen")
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Upload Video", tint = MaterialTheme.colorScheme.secondary)
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
    val exoPlayer = remember(context, videoData.videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoData.videoUrl))
            prepare()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
            onPlay()
        } else {
            exoPlayer.pause()
            onPause()
        }
    }

    DisposableEffect(exoPlayer) {
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
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

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

    val userName = remember { mutableStateOf("Loading...") }
    val profileImageUrl = remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()
    LaunchedEffect(videoData.userId) {
        videoData.userId.let { uid ->
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userName.value = snapshot.getString("name") ?: "User"
                        profileImageUrl.value = snapshot.getString("imageUrl") ?: ""
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row { Text(videoData.caption, color = MaterialTheme.colorScheme.secondary) }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = profileImageUrl.value,
                    contentDescription = "User Profile",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(userName.value, color = MaterialTheme.colorScheme.surface)
                Spacer(modifier = Modifier.width(12.dp))
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
    }
}

// Fetch only video metadata (excluding the URL)
fun fetchVideoMetadataFromFirebase(onVideoMetadataLoaded: (List<VideoMetadata>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("videos")
        .get()
        .addOnSuccessListener { result ->
            val videoMetadataList = result.documents.map { doc ->
                VideoMetadata(
                    id = doc.id,
                    caption = doc.getString("caption") ?: "",
                    userId = doc.getString("userId") ?: "",
                    likes = doc.getLong("likes")?.toInt() ?: 0
                )
            }
            onVideoMetadataLoaded(videoMetadataList)
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error getting video documents: $e")
        }
}

// Fetch the video URL for a specific video ID
fun fetchVideoUrl(videoId: String, onVideoUrlFetched: (Uri?) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child("videos/$videoId.mp4")
    storageRef.downloadUrl
        .addOnSuccessListener { uri ->
            onVideoUrlFetched(uri)
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error getting download URL for $videoId: $e")
            onVideoUrlFetched(null)
        }
}