import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun CarbonFootprintScreen(userId: String, modifier: Modifier = Modifier) {
    // State variables to hold the fetched values
    var totalCarbonSavings by remember { mutableStateOf(0.0) }
    var totalMoneySaved by remember { mutableStateOf(0.0) }

    // Remember a snapshot listener registration so we can remove it when the composable leaves the composition
    var snapshotListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Use a side effect to register the real-time listener when the composable is first launched
    DisposableEffect(userId) {
        // Set up the Firestore real-time listener
        snapshotListener = setupRealTimeListener(userId) { carbonSavings, moneySaved ->
            totalCarbonSavings = carbonSavings
            totalMoneySaved = moneySaved
        }

        // Clean up the listener when the composable leaves the composition
        onDispose {
            snapshotListener?.remove()
        }
    }

    // Composable UI
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {
        Text(
            text = "Your Carbon Footprint Impact:",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Money saved")
                Text(text = "$${totalMoneySaved}")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "CO2e Reduced")
                Text(text = "${totalCarbonSavings} kg CO2e")
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
