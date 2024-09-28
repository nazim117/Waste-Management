package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    userId: String,
    challengeViewModel: ChallengeViewModel,
    onNavigateToQuizAndLeaderboard: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            UserStreakScreen(userId = userId, firestore = FirebaseFirestore.getInstance())
        }

        item {
            challengeViewModel.WeeklyUserChallengeScreen(viewModel = challengeViewModel, userId = userId)
        }

        item {
            CarbonFootprintScreen(userId = userId)
        }
    }
}