package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun CarbonFootprintScreen(userId: String, modifier: Modifier = Modifier) {
    var totalCarbonSavings by remember { mutableStateOf(0.0) }
    var totalMoneySaved by remember { mutableStateOf(0.0) }
    var snapshotListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    DisposableEffect(userId) {
        snapshotListener = setupRealTimeListener(userId) { carbonSavings, moneySaved ->
            totalCarbonSavings = carbonSavings
            totalMoneySaved = moneySaved
        }

        onDispose {
            snapshotListener?.remove()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {
        Text(
            text = "Your Carbon Footprint Impact:",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF2E7D32),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Money Saved", style = MaterialTheme.typography.bodyLarge)
                Text(text = "$${totalMoneySaved}", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF388E3C))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "CO2e Reduced", style = MaterialTheme.typography.bodyLarge)
                Text(text = "${totalCarbonSavings} kg CO2e", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF66BB6A))
            }
        }
    }
}

// Function to set up the real-time Firestore listener
private fun setupRealTimeListener(userId: String, onDataChanged: (Double, Double) -> Unit): ListenerRegistration {
    val firestore = FirebaseFirestore.getInstance()

    return firestore.collection("leaderboard")
        .whereEqualTo("userId", userId)
        .addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                exception.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val document = snapshots.documents[0]
                val carbonSavings = document.getDouble("carbonSavings") ?: 0.0
                val moneySaved = document.getDouble("moneySaved") ?: 0.0
                onDataChanged(carbonSavings, moneySaved)
            }
        }
}