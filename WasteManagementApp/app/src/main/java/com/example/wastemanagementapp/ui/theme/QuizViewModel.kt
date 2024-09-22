package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
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

    private var _currentQuestionIndex = mutableStateOf(0)
    val currentQuestionIndex: State<Int> = _currentQuestionIndex

    private var _score = mutableStateOf(0)
    val score: State<Int> = _score

    private var _quizFinished = mutableStateOf(false)
    val quizFinished: State<Boolean> = _quizFinished

    init {
        fetchQuizQuestions()
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

    fun submitAnswer(selectedAnswer: Int) {
        viewModelScope.launch {
            if(selectedAnswer == questions[currentQuestionIndex.value].correctAnswer){
                _score.value++
            }
            if(currentQuestionIndex.value < questions.size - 1){
                _currentQuestionIndex.value++
            }else {
                _quizFinished.value = true
            }
        }
    }

}
@Composable
fun QuizScreen(viewModel: QuizViewModel) {
    val questions = viewModel.questions

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No quiz questions available.")
        }
    } else {
        // Render the quiz UI
        QuizQuestion(
            question = questions[viewModel.currentQuestionIndex.value],
            onAnswerSelected = { viewModel.submitAnswer(it) }
        )
    }
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
                    style = MaterialTheme.typography.bodySmall.merge(),
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

private @Composable
fun QuizResult(score: Int, totalQuestions: Int) {
    Column(
        modifier =  Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Finished!",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your score $score out of $totalQuestions",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
