package com.example.wastemanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wastemanagementapp.ui.theme.CarbonFootprintScreen
import com.example.wastemanagementapp.ui.theme.QuizScreen
import com.example.wastemanagementapp.ui.theme.QuizViewModel
import com.example.wastemanagementapp.ui.theme.ScoreSubmissionScreen
import com.example.wastemanagementapp.ui.theme.WeeklyUserChallengeScreen
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Weekly User Challenge (1st)
                    WeeklyUserChallengeScreen()

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quiz Screen (2nd)
                    val viewModel = viewModel<QuizViewModel>()
                    QuizScreen(viewModel, userId, firestore)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Carbon Footprint (3rd)
                    CarbonFootprintScreen()

                    Spacer(modifier = Modifier.height(32.dp))

                    // Leaderboard (4th)
                    ScoreSubmissionScreen(firestore = firestore)
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