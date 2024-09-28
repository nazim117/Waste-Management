package com.example.wastemanagementapp

import CarbonFootprintScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wastemanagementapp.ui.theme.ChallengeViewModel
import com.example.wastemanagementapp.ui.theme.Leaderboard
import com.example.wastemanagementapp.ui.theme.QuizScreen
import com.example.wastemanagementapp.ui.theme.QuizViewModel
import com.example.wastemanagementapp.ui.theme.UserStreakScreen
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        val userId = getUserId()

        setContent {
            // Get all view models
            val challengeViewModel: ChallengeViewModel = viewModel()
            val quizViewModel: QuizViewModel = viewModel()

            // Check if any data is still loading
            val isLoading = challengeViewModel.loading || quizViewModel.loading
            // Add other view models' loading states if needed, e.g. leaderboard loading

            // Render the UI
            Surface(color = MaterialTheme.colorScheme.background) {
                if (isLoading) {
                    // Full-screen loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Show actual content once data is loaded
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            UserStreakScreen(userId = userId, firestore = firestore)
                        }

                        item {
                            // Weekly User Challenge
                            challengeViewModel.WeeklyUserChallengeScreen(viewModel = challengeViewModel, userId = userId)
                        }

                        item {
                            // Quiz Screen
                            QuizScreen(viewModel = quizViewModel, userId = userId, firestore = firestore)
                        }

                        item {
                            // Carbon Footprint Screen
                            CarbonFootprintScreen(userId = userId)
                        }

                        item {
                            // Leaderboard
                            Leaderboard(firestore = firestore)
                        }
                    }
                }
            }
        }
    }

    private fun getUserId(): String {
        val prefs = getPreferences(MODE_PRIVATE)
        var userId = prefs.getString("userId", null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit().putString("userId", userId).apply()
        }
        return userId
    }
}