package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    private val _leaderboard = mutableStateListOf<LeaderboardEntry>()
    val leaderboard: List<LeaderboardEntry> = _leaderboard

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
            firestore.collection("quizzes")
                .document("waste_management")
                .collection("questions")
                .get()
                .addOnSuccessListener { result ->
                    val fetchedQuestions = result.mapNotNull { document ->
                        document.toObject(QuizQuestion::class.java)
                    }
                    _questions.addAll(fetchedQuestions)
                }
                .addOnFailureListener{ exception ->
                    viewModelScope.launch (Dispatchers.Main){
                        exception.printStackTrace()
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
                        }else {
                            val documentId = documents.documents[0].id
                            val currentFirestoreScore = documents.documents[0].getLong("score")?.toInt() ?:0
                            val updatedScore = currentFirestoreScore + _score.value
                            println("updated score $updatedScore")
                            firestore.collection("leaderboard")
                                .document(documentId)
                                .update("score", updatedScore)
                        }
                    }
                    .addOnFailureListener{ exception ->
                        exception.printStackTrace()
                    }
            }
            fetchLeaderboard(firestore)
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
    val currentQuestionIndex by viewModel.currentQuestionIndex
    val questions = viewModel.questions
    val quizFinished by viewModel.quizFinished
    val score by viewModel.score

    var showDialog by remember { mutableStateOf(false) }

    if(quizFinished && !showDialog){
        showDialog = true
        println("Dialog shown")
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz questions available.")
        }
    } else {
        // Render the quiz UI
        QuizQuestion(
            question = questions[viewModel.currentQuestionIndex.value],
            onAnswerSelected = { viewModel.submitAnswer(it, userId, firestore) }
        )
    }

    if(showDialog){
        QuizFinishedDialog(
            score = score,
            totalQuestions = questions.size,
            onDismiss = {
                showDialog = false
                viewModel.resetQuizFinished()
                println("Close button clicked: showdialog = $showDialog")
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
            Text(text = "Quiz Finished!")
        },
        text = {
            Column {
                Text(text = "Your score is $score out of $totalQuestions")
                Spacer(modifier = Modifier.height(16.dp))
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = question.question, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        question.options.forEachIndexed{ index, option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == index,
                        onClick = { selectedOption = index}
                    )
                    .padding(vertical = 8.dp)
            ){
                RadioButton(
                    selected = selectedOption == index,
                    onClick = { selectedOption = index }
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge.merge(),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                selectedOption?.let { onAnswerSelected(it)}
                selectedOption = null
            },
            enabled = selectedOption != null
        ){
            Text("Submit")
        }
    }
}