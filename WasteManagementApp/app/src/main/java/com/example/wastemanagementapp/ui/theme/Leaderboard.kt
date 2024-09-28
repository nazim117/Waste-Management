package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wastemanagementapp.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

@Composable
fun Leaderboard(firestore: FirebaseFirestore) {
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }

    var snapshotListener by remember { mutableStateOf<ListenerRegistration?>(null) }

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
    ){
        Text(
            text = "Leaderboard:",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ){
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
}

fun setupLeaderBoardListener(
    firestore: FirebaseFirestore,
    onDataChanged: (List<LeaderboardEntry>) -> Unit
): ListenerRegistration? {
    return firestore.collection("leaderboard")
        .orderBy("score", Query.Direction.DESCENDING)
        .limit(10)
        .addSnapshotListener { snapshots, exception ->
            if(exception != null) {
                exception.printStackTrace()
                return@addSnapshotListener
            }

            if(snapshots != null){
                val leaderboard = snapshots.documents.map { document ->
                    LeaderboardEntry(
                        userId = document.getString("userId") ?: "",
                        score = document.getLong("score")?.toInt() ?: 0
                    )
                }
                onDataChanged(leaderboard)
            }
        }
}