package com.example.wastemanagementapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun ScoreSubmissionScreen(userId: String, firestore: FirebaseFirestore) {
    var scoreInput by remember { mutableStateOf("") }
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }
    val coroutineScope = rememberCoroutineScope()

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
            text = "Your ID: ${userId.take(8)}",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = scoreInput,
            onValueChange = {scoreInput = it},
            label = {Text("Enter your score")},
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val score = scoreInput.toIntOrNull() ?: 0
                coroutineScope.launch(Dispatchers.IO) {
                    submitScore(firestore, userId, score)
                    updateLeaderboard(firestore) { newLeaderboard ->
                        coroutineScope.launch(Dispatchers.Main) {
                            leaderboard = newLeaderboard
                        }
                    }
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ){
            Text(text = "Submit Score")
        }

        Text(
            text = "Leaderboard:",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        leaderboard.forEachIndexed{ index, entry ->
            Text(
                text = "${index + 1}. ${entry.userId.take(8)}... - ${entry.score}",
                fontSize = 16.sp,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

private suspend fun submitScore(
    fireStore: FirebaseFirestore,
    userId: String,
    score: Int
) {
    val scoreData = hashMapOf(
        "userId" to userId,
        "score" to score
    )
    fireStore.collection("leaderboard")
        .add(scoreData)
}

private fun updateLeaderboard(
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
