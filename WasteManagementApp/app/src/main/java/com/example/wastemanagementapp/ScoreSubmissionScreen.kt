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

@Composable
fun ScoreSubmissionScreen(userId: String, database: LeaderboardDatabase) {
    var scoreInput by remember { mutableStateOf("") }
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        updateLeaderboard(database, leaderboardUpdater = {leaderboard = it})
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
                    submitScore(database, userId, score)
                    updateLeaderboard(database) { newLeaderboard ->
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
    database: LeaderboardDatabase,
    userId: String,
    score: Int
) {
    database.leaderboardDao().insertScore(LeaderboardEntry(userId, score))
}

private suspend fun updateLeaderboard(
    database: LeaderboardDatabase,
    leaderboardUpdater: (List<LeaderboardEntry>) -> Unit
) {
    val leaderboard = database.leaderboardDao().getTopScores(10)
    leaderboardUpdater(leaderboard)
}
