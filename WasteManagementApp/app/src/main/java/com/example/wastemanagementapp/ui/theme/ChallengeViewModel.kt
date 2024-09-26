package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Challenge(
    val id: String = "",
    val challenge: String = ""
)

class ChallengeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _challenges = mutableStateListOf<Challenge>()
    val challenges: List<Challenge> = _challenges

    // State for the current challenge index
    private var _currentChallengeIndex = mutableStateOf(0)
    val currentChallengeIndex: Int get() = _currentChallengeIndex.value

    // State to check if all challenges are completed
    private var _challengesCompleted = mutableStateOf(false)
    val challengesCompleted: Boolean get() = _challengesCompleted.value

    init {
        fetchChallenges()
    }

    private fun fetchChallenges() {
        viewModelScope.launch(Dispatchers.IO) {
            firestore.collection("challenges")
                .document("waste_management")
                .collection("tasks")
                .get()
                .addOnSuccessListener { result ->
                    val fetchedChallenges = result.mapNotNull { document ->
                        document.toObject(Challenge::class.java)
                    }
                    _challenges.clear()
                    _challenges.addAll(fetchedChallenges)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun submitChallenge() {
        if (_currentChallengeIndex.value < challenges.size - 1) {
            _currentChallengeIndex.value++
        } else {
            _challengesCompleted.value = true
        }
    }

    @Composable
    fun WeeklyUserChallengeScreen() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9))
                .padding(16.dp)
        ) {
            Text(
                text = "Weekly Waste Challenge",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (challengesCompleted) {
                Text(
                    text = "Challenges Completed!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                if (challenges.isNotEmpty()) {
                    val currentChallenge = challenges[currentChallengeIndex]
                    Text(
                        text = currentChallenge.challenge,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text("No challenges available.")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.Gray)
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.Gray)
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { submitChallenge() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}