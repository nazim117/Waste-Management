package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wastemanagementapp.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class QuizQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = listOf(),
    val correctAnswer:Int = 0
)

class QuizViewModel: ViewModel(){
    private val firestore = FirebaseFirestore.getInstance()
    private val _questions = mutableStateListOf<QuizQuestion>()
    val questions: List<QuizQuestion> = _questions

    private var _loading = mutableStateOf(true)
    val loading: Boolean get() = _loading.value

    private val _leaderboard = mutableStateListOf<LeaderboardEntry>()

    private var _currentQuestionIndex = mutableStateOf(0)
    val currentQuestionIndex: State<Int> = _currentQuestionIndex

    private var _score = mutableStateOf(0)
    val score: State<Int> = _score

    private var _quizFinished = mutableStateOf(false)
    val quizFinished: State<Boolean> = _quizFinished

    init {
        fetchQuizQuestions()
    }

    fun resetQuizFinished(){
        _quizFinished.value = false
    }

    private fun fetchQuizQuestions() {
        viewModelScope.launch(Dispatchers.IO){
            _loading.value = true
            firestore.collection("quizzes")
                .document("waste_management")
                .collection("questions")
                .get()
                .addOnSuccessListener { result ->
                    val fetchedQuestions = result.mapNotNull { document ->
                        document.toObject(QuizQuestion::class.java)
                    }
                    _questions.clear()
                    _questions.addAll(fetchedQuestions)
                    _loading.value = false
                }
                .addOnFailureListener{ exception ->
                    viewModelScope.launch (Dispatchers.Main){
                        exception.printStackTrace()
                        _loading.value = false
                    }
                }
        }
    }

    fun submitAnswer(selectedAnswer: Int, userId: String, firestore: FirebaseFirestore) {
        viewModelScope.launch(Dispatchers.IO) {
            if(selectedAnswer == questions[currentQuestionIndex.value].correctAnswer){
                _score.value++

                firestore.collection("leaderboard")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        if(documents.isEmpty){
                            firestore.collection("leaderboard").add(
                                hashMapOf(
                                    "userId" to userId,
                                    "score" to _score.value
                                )
                            )
                        } else {
                            val documentId = documents.documents[0].id

                            val currentFirestoreScore = documents.documents[0].getLong("score")?.toInt() ?:0

                            val updatedScore = currentFirestoreScore + _score.value
                            println("updated score $updatedScore")
                            firestore.collection("leaderboard")
                                .document(documentId)
                                .update("score", updatedScore)
                        }
                        fetchLeaderboard(firestore)
                    }
                    .addOnFailureListener{ exception ->
                        exception.printStackTrace()
                    }
            }
            if(currentQuestionIndex.value < questions.size - 1){
                _currentQuestionIndex.value++
            }else {
                _quizFinished.value = true
            }
        }
    }

    private fun fetchLeaderboard(firestore: FirebaseFirestore) {
        viewModelScope.launch(Dispatchers.IO) {
            firestore.collection("leaderboard")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener { result ->
                    val newLeaderboard = result.map { document ->
                        LeaderboardEntry (
                            userId = document.getString("userId") ?: "",
                            score = document.getLong("score")?.toInt() ?: 0
                        )
                    }
                    _leaderboard.clear()
                    _leaderboard.addAll(newLeaderboard)
                }
        }
    }
}

@Composable
fun QuizScreen(viewModel: QuizViewModel, userId: String, firestore: FirebaseFirestore) {
    val questions = viewModel.questions
    val quizFinished by viewModel.quizFinished
    val score by viewModel.score
    var showDialog by remember { mutableStateOf(false) }

    if (quizFinished && !showDialog) {
        showDialog = true
    }

    // Check if there are questions to display
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz questions available.")
        }
    } else {
        // Display the current quiz question
        QuizQuestion(
            question = questions[viewModel.currentQuestionIndex.value],
            onAnswerSelected = { viewModel.submitAnswer(it, userId, firestore) }
        )
    }

    // Display the dialog when the quiz is finished
    if (showDialog) {
        QuizFinishedDialog(
            score = score,
            totalQuestions = questions.size,
            onDismiss = {
                showDialog = false
                viewModel.resetQuizFinished()
            },
        )
    }
}


@Composable
fun QuizFinishedDialog(
    score: Int,
    totalQuestions: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Quiz Finished!", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                Text(
                    text = "Your score is $score out of $totalQuestions",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


@Composable
fun QuizQuestion(question: QuizQuestion, onAnswerSelected: (Int) -> Unit) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }

    // Use an OutlinedCard for visual separation and a more polished look
    androidx.compose.material3.OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // Add padding around the card to keep space from the screen edges
            .background(Color(0xFFE8F5E9)) // Optional light background
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Padding inside the card for spacing
        ) {
            // Display the question
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display answer options with radio buttons
            question.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedOption == index,
                            onClick = { selectedOption = index }
                        )
                        .padding(vertical = 8.dp) // Add space between the options
                ) {
                    RadioButton(
                        selected = selectedOption == index,
                        onClick = { selectedOption = index },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp) // Space between radio button and text
                    )
                }
            }

            // Submit button
            Button(
                onClick = {
                    selectedOption?.let { onAnswerSelected(it) }
                    selectedOption = null // Reset selection after submission
                },
                enabled = selectedOption != null, // Disable button until an option is selected
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Submit")
            }
        }
    }
}
