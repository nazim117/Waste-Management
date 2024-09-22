package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import com.example.wastemanagementapp.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun ScoreSubmissionScreen(firestore: FirebaseFirestore) {
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }

    LaunchedEffect(Unit) {
        updateLeaderboard(firestore){newLeaderboard ->
            leaderboard = newLeaderboard
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Text(
            text = "Leaderboard:",
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        LazyColumn{
            items(leaderboard.size) { index ->
                val entry = leaderboard[index]
                Text(
                    text = "${index + 1}. ${entry.userId.take(8)}... - ${entry.score}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

fun updateLeaderboard(
    firestore: FirebaseFirestore,
    leaderboardUpdater: (List<LeaderboardEntry>) -> Unit
) {
    firestore.collection("leaderboard")
        .orderBy("score", Query.Direction.DESCENDING)
        .limit(10)
        .get()
        .addOnSuccessListener { result ->
            val leaderboard = result.map { document ->
                LeaderboardEntry (
                    userId = document.getString("userId") ?: "",
                    score = document.getLong("score")?.toInt() ?: 0
                )
            }
            leaderboardUpdater(leaderboard)
        }
}
