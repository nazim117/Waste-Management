package com.example.wastemanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wastemanagementapp.ui.theme.ChallengeViewModel
import com.example.wastemanagementapp.ui.theme.HomeScreen
import com.example.wastemanagementapp.ui.theme.QuizAndLeaderboardScreen
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
            val challengeViewModel: ChallengeViewModel = viewModel()
            val quizViewModel: QuizViewModel = viewModel()

            // Initialize state for navigation
            var currentScreen by remember { mutableStateOf(WasteManagementScreen.Home) }

            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen -> currentScreen = screen }
                    )
                }
            ) { innerPadding ->
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    when (currentScreen) {
                        WasteManagementScreen.Home -> HomeScreen(
                            userId = userId,
                            challengeViewModel = challengeViewModel,
                            onNavigateToQuizAndLeaderboard = { currentScreen = WasteManagementScreen.QuizAndLeaderboard }
                        )
                        WasteManagementScreen.QuizAndLeaderboard -> QuizAndLeaderboardScreen(
                            userId = userId,
                            quizViewModel = quizViewModel,
                            firestore = firestore,
                            onNavigateBack = { currentScreen = WasteManagementScreen.Home }
                        )
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

@Composable
fun BottomNavigationBar(currentScreen: WasteManagementScreen, onScreenSelected: (WasteManagementScreen) -> Unit) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        BottomNavigationItem(
            selected = currentScreen == WasteManagementScreen.Home,
            onClick = { onScreenSelected(WasteManagementScreen.Home) },
            label = { Text("Home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )
        BottomNavigationItem(
            selected = currentScreen == WasteManagementScreen.QuizAndLeaderboard,
            onClick = { onScreenSelected(WasteManagementScreen.QuizAndLeaderboard) },
            label = { Text("Quiz & Leaderboard") },
            icon = { Icon(Icons.Default.List, contentDescription = "Quiz & Leaderboard") }
        )
    }
}