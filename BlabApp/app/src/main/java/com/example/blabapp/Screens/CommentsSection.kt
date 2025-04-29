package com.example.blabapp.Screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

data class CommentWithUser(
    val comment: String,
    val name: String
)

@Composable
fun CommentSection(
    videoId: String,
    userId: String,
    onBack: () -> Unit,
    onSubmitComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(listOf<CommentWithUser>()) }

    fun refreshComments() {
        fetchCommentsFromFirebase(videoId) { commentList ->
            comments = commentList
        }
    }

    LaunchedEffect(videoId) {
        refreshComments()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Comments", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No comments yet.", color = MaterialTheme.colorScheme.surface)
            }
        } else {

            LazyColumn(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

            items(comments) { comment ->
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(comment.name)
                            }
                            append(": ${comment.comment}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.background),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = { Text("Add a Comment") },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            postCommentToFirestore(
                                videoId = videoId,
                                userId = userId,
                                commentText = commentText,
                                onSuccess = {
                                    refreshComments()
                                    commentText = ""
                                },
                                onFailure = { }
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Comment",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            singleLine = true
        )
    }
}

fun fetchCommentsFromFirebase(videoId: String, onComplete: (List<CommentWithUser>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val commentsRef = db.collection("videos").document(videoId).collection("comments")

    commentsRef
        .whereNotEqualTo("timestamp", null) // ⬅️ Filter out null timestamps
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .get()
        .addOnSuccessListener { result ->
            val comments = mutableListOf<CommentWithUser>()
            val tasks = mutableListOf<Task<DocumentSnapshot>>()

            for (document in result.documents) {
                val commentText = document.getString("comment")
                val userId = document.getString("userId")

                if (commentText == null || userId == null) {
                    Log.w("FirestoreDebug", "Missing fields in comment: $document")
                    continue
                }

                val userTask = db.collection("users").document(userId).get()
                    .addOnSuccessListener { userSnapshot ->
                        val username = userSnapshot.getString("name") ?: "Unknown"
                        Log.d("FirestoreDebug", "Fetched user name: $username")
                        comments.add(CommentWithUser(commentText, username))
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreDebug", "Failed to fetch user $userId", e)
                    }

                tasks.add(userTask)
            }

            Tasks.whenAllComplete(tasks)
                .addOnSuccessListener {
                    Log.d("FirestoreDebug", "All user fetches completed. Returning ${comments.size} comments.")
                    onComplete(comments)
                }
        }
        .addOnFailureListener {
            Log.e("FirestoreDebug", "Error fetching comments", it)
            onComplete(emptyList())
        }
}

fun postCommentToFirestore(videoId: String, userId: String, commentText: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val comment = hashMapOf(
        "comment" to commentText,
        "userId" to userId,
        "timestamp" to FieldValue.serverTimestamp()
    )

    db.collection("videos").document(videoId)
        .collection("comments")
        .add(comment)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}
