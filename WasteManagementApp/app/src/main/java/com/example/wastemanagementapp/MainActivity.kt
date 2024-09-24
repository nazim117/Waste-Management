package com.example.wastemanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wastemanagementapp.ui.theme.QuizScreen
import com.example.wastemanagementapp.ui.theme.QuizViewModel
import com.example.wastemanagementapp.ui.theme.ScoreSubmissionScreen
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        val userId = getUserId()

        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    val viewModel = viewModel<QuizViewModel>()
                    QuizScreen(viewModel, userId, firestore = firestore)

                    Spacer(modifier = Modifier.height(32.dp))

                    ScoreSubmissionScreen(
                        firestore = firestore
                    )
                }
            }
        }
    }

    private fun getUserId(): String {
        val prefs = getPreferences(MODE_PRIVATE)
        var userId = prefs.getString("userId", null)
        if(userId == null){
            userId = UUID.randomUUID().toString()
            prefs.edit().putString("userId", userId).apply()
        }
        return userId
    }
}