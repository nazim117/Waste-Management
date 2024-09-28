package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
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
import androidx.compose.ui.draw.shadow
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
    val challenge: String = "",
    val carbonSavings: Double = 0.0,
    val moneySaved: Double = 0.0
)

class ChallengeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _challenges = mutableStateListOf<Challenge>()
    private val challenges: List<Challenge> = _challenges

    private var _streak = mutableStateOf(0)
    private val streak: Int get() = _streak.value

    private var _loading = mutableStateOf(true)
    val loading: Boolean get() = _loading.value

    private var _currentChallengeIndex = mutableStateOf(0)
    private val currentChallengeIndex: Int get() = _currentChallengeIndex.value

    private var _challengesCompleted = mutableStateOf(false)
    private val challengesCompleted: Boolean get() = _challengesCompleted.value

    private var _currentChallengeProgress = mutableStateOf(0)
    private val currentChallengeProgress: Int get() = _currentChallengeProgress.value

    private var _currentChallengeFinished = mutableStateOf(false)
    private val currentChallengeFinished: Boolean get() = _currentChallengeFinished.value

    private var _totalCarbonSavings = mutableStateOf(0.0)
    private val totalCarbonSavings: Double get() = _totalCarbonSavings.value

    private var _totalMoneySaved = mutableStateOf(0.0)

    private var _score = mutableStateOf(0)
    private val score: Int get() = _score.value

    init {
        fetchChallenges()
    }


    private fun fetchChallenges() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
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
                    _loading.value = false

                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    _loading.value = false
                }
        }
    }

    fun completeChallengeAction(userId: String) {
        if (!_currentChallengeFinished.value) {
            if (_currentChallengeProgress.value < 3) {
                _currentChallengeProgress.value++
            }

            if (_currentChallengeProgress.value == 3) {
                _currentChallengeFinished.value = true

                val currentChallenge = challenges[currentChallengeIndex]
                _totalCarbonSavings.value += currentChallenge.carbonSavings
                _totalMoneySaved.value += currentChallenge.moneySaved
                _score.value += 1 // Increment score for the user
                firestore.collection("leaderboard")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        if(documents.isEmpty){
                            firestore.collection("leaderboard").add(
                                hashMapOf(
                                    "userId" to userId,
                                    "score" to _score.value,
                                    "carbonSavings" to _totalCarbonSavings.value,
                                    "moneySaved" to _totalMoneySaved.value,
                                    "streak" to _streak.value
                                )
                            )
                        } else {
                            val documentId = documents.documents[0].id

                            val currentFirestoreScore = documents.documents[0].getLong("score")?.toInt() ?:0
                            val currentFirestoreCarbonSavings = documents.documents[0].getLong("carbonSavings")?.toDouble() ?:0.0
                            val currentFirestoreMoneySaved = documents.documents[0].getLong("moneySaved")?.toInt() ?:0
                            val currentFirestoreStreak = documents.documents[0].getLong("streak")?.toInt() ?:0

                            val updatedScore = currentFirestoreScore + 1
                            val updatedCarbonSavings = currentFirestoreCarbonSavings + _totalCarbonSavings.value
                            val updatedMoneySaved = currentFirestoreMoneySaved + _totalMoneySaved.value
                            val updatedStreak = currentFirestoreStreak + 1

                            println("updated score $updatedScore")
                            println("updated carbonSavings $updatedCarbonSavings")
                            println("updated MoneySaved $updatedMoneySaved")
                            println("updated Streak $updatedStreak")

                            val updates = mapOf(
                                "score" to updatedScore,
                                "carbonSavings" to updatedCarbonSavings,
                                "moneySaved" to updatedMoneySaved,
                                "streak" to updatedStreak
                            )

                            // Perform the update
                            firestore.collection("leaderboard")
                                .document(documentId)
                                .update(updates)
                                .addOnSuccessListener {
                                    println("Successfully updated user data.")
                                }
                                .addOnFailureListener { exception ->
                                    exception.printStackTrace()
                                }
                        }
                    }
                    .addOnFailureListener{ exception ->
                        exception.printStackTrace()
                    }
            }
        } else {
            submitChallenge(userId)
        }
    }

    private fun submitChallenge(userId: String) {
        _currentChallengeProgress.value = 0
        _currentChallengeFinished.value = false

        _streak.value += 1
        if (_currentChallengeIndex.value < challenges.size - 1) {
            _currentChallengeIndex.value++
        } else {
            _challengesCompleted.value = true
        }

        // Save new data to Firestore after completing a challenge
        saveDataToFirestore(userId)
    }

    private fun saveDataToFirestore(userId: String) {
        val userDocRef = firestore.collection("leaderboard").document(userId)

        // Update the user's carbonSavings and score
        userDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // User already exists, update the document
                val currentCarbonSavings = documentSnapshot.getDouble("carbonSavings") ?: 0.0
                val currentScore = documentSnapshot.getLong("score")?.toInt() ?: 0
                val currentStreak = documentSnapshot.getLong("streak")?.toInt() ?: 0

                userDocRef.update(
                    mapOf(
                        "carbonSavings" to currentCarbonSavings + totalCarbonSavings,
                        "score" to currentScore + score,
                        "streak" to currentStreak + 1
                    )
                )
            } else {
                // Create a new document for the user
                userDocRef.set(
                    mapOf(
                        "userId" to userId,
                        "carbonSavings" to totalCarbonSavings,
                        "score" to score,
                        "streak" to streak
                    )
                )
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }
    @Composable
    fun WeeklyUserChallengeScreen(viewModel: ChallengeViewModel, userId: String) {
        val challenges = viewModel.challenges
        val challengesCompleted = viewModel.challengesCompleted

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
                    val currentChallenge = challenges[viewModel.currentChallengeIndex]
                    Text(
                        text = currentChallenge.challenge,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3) { index ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Star",
                                tint = if (index < viewModel.currentChallengeProgress) Color.Yellow else Color.Gray
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = viewModel.currentChallengeProgress / 3f,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                } else {
                    Text("No challenges available.")
                }

                Button(
                    onClick = { viewModel.completeChallengeAction(userId) },
                    modifier = Modifier.fillMaxWidth().shadow(4.dp)
                ) {
                    if (viewModel.currentChallengeFinished) {
                        Text(text = "Next Challenge")
                    } else {
                        Text(text = "Submit")
                    }
                }
            }
        }
    }
}