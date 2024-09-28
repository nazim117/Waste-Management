package com.example.wastemanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wastemanagementapp.ui.theme.CarbonFootprintScreen
import com.example.wastemanagementapp.ui.theme.ChallengeViewModel
import com.example.wastemanagementapp.ui.theme.Leaderboard
import com.example.wastemanagementapp.ui.theme.QuizScreen
import com.example.wastemanagementapp.ui.theme.QuizViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        val userId = getUserId()

        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        // Weekly User Challenge (1st)
                        val challengeViewModel = viewModel<ChallengeViewModel>()
                        challengeViewModel.WeeklyUserChallengeScreen(viewModel = challengeViewModel, userId = userId, firestore = firestore) // Call directly from ViewModel
                    }

                    item {
                        // Quiz Screen (2nd)
                        val quizViewModel = viewModel<QuizViewModel>()
                        QuizScreen(viewModel = quizViewModel, userId = userId, firestore = firestore)
                    }

                    item {
                        // Carbon Footprint (3rd)
                        CarbonFootprintScreen(userId = userId)
                    }

                    item {
                        // Leaderboard (4th)
                        Leaderboard(firestore = firestore)
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