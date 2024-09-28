package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun UserStreakScreen(userId: String, firestore: FirebaseFirestore) {
    var streak by remember { mutableStateOf(0) }
    var snapshotListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    DisposableEffect(Unit) {
        snapshotListener = setupStreakListener(firestore, userId) { newStreak ->
            streak = newStreak
        }

        onDispose {
            snapshotListener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Current Streak",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "$streak Days",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

fun setupStreakListener(
    firestore: FirebaseFirestore,
    userId: String,
    onStreakChanged: (Int) -> Unit
): ListenerRegistration? {
    println("Setting up listener for userId field: $userId") // Debug log

    return firestore.collection("leaderboard")
        .whereEqualTo("userId", userId) // Query by the userId field instead of document ID
        .limit(1) // Since we expect only one document per userId, limit to 1 result
        .addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                println("Error fetching streak: ${exception.message}")
                exception.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val document = snapshots.documents[0]
                val newStreak = document.getLong("streak")?.toInt() ?: 0
                println("Fetched streak: $newStreak for userId: $userId")
                onStreakChanged(newStreak)
            } else {
                println("No document found for userId: $userId")
            }
        }
}