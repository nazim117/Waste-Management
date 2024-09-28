package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wastemanagementapp.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

@Composable
fun Leaderboard(firestore: FirebaseFirestore, currentUserId: String) {
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }

    var snapshotListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Firestore listener to fetch leaderboard data
    DisposableEffect(Unit) {
        snapshotListener = setupLeaderBoardListener(firestore) { newLeaderboard ->
            leaderboard = newLeaderboard
        }

        onDispose {
            snapshotListener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        if (leaderboard.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No data available", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)  // Fixed height to avoid infinite height constraints
            ) {
                items(leaderboard) { entry ->
                    LeaderboardEntryCard(rank = leaderboard.indexOf(entry) + 1, entry)
                }
            }
        }
    }
}

@Composable
fun LeaderboardEntryCard(rank: Int, entry: LeaderboardEntry) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "$rank.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "${entry.userId.take(8)}...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "${entry.score}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun setupLeaderBoardListener(
    firestore: FirebaseFirestore,
    onDataChanged: (List<LeaderboardEntry>) -> Unit
): ListenerRegistration? {
    return firestore.collection("leaderboard")
        .orderBy("score", Query.Direction.DESCENDING)
        .limit(10)
        .addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                exception.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshots != null && snapshots.documents.isNotEmpty()) {
                val leaderboard = snapshots.documents.map { document ->
                    LeaderboardEntry(
                        userId = document.getString("userId") ?: "",
                        score = document.getLong("score")?.toInt() ?: 0
                    )
                }
                onDataChanged(leaderboard)
            } else {
                onDataChanged(emptyList())
            }
        }
}