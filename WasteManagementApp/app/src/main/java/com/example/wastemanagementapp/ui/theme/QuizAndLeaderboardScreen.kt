package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun QuizAndLeaderboardScreen(
    userId: String,
    quizViewModel: QuizViewModel,
    firestore: FirebaseFirestore,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val showCongratsDialog = remember { mutableStateOf(false) }

    // Firestore listener for leaderboard and check if the user is #1
    DisposableEffect(Unit) {
        val listener = firestore.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(1) // Only get the top user
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    exception.printStackTrace()
                    return@addSnapshotListener
                }
                snapshots?.documents?.firstOrNull()?.let { document ->
                    val topUserId = document.getString("userId")
                    if (topUserId == userId) {
                        showCongratsDialog.value = true
                    }
                }
            }
        onDispose {
            listener.remove()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                QuizScreen(viewModel = quizViewModel, userId = userId, firestore = firestore)
            }

            item {
                Leaderboard(firestore = firestore, currentUserId = userId)
            }
        }

        // Show the custom dialog on top of the content when the user is #1
        if (showCongratsDialog.value) {
            CustomCongratsDialog(
                onDismiss = { showCongratsDialog.value = false },
                title = "Congratulations!",
                message = "You're #1 on the leaderboard! A bunch of trees have been planted in your name.",
                buttonText = "Awesome!"
            )
        }
    }
}