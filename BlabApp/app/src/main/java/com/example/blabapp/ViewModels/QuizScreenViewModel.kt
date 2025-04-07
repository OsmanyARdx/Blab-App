package com.example.blabapp.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blabapp.Nav.QuizQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.Normalizer

class QuizScreenViewModel : ViewModel() {
    var quizQuestions = mutableStateOf<List<QuizQuestion>>(emptyList())
        private set

    var isLoading = mutableStateOf(true)
        private set

    fun loadQuizQuestions(userId: String, moduleId: String) {
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance().collection("users")
                    .document(userId).get().await()
                val learningPreference = userDoc.getString("learning") ?: "ES"
                val quizCollection = if (learningPreference == "ES") "quizForLearningES" else "quizForLearningEN"

                val quizSnapshot = FirebaseFirestore.getInstance().collection("modules")
                    .document(moduleId).collection("quizes").get().await()

                val allQuestions = mutableListOf<QuizQuestion>()
                for (quizDoc in quizSnapshot.documents) {
                    val subCollectionSnapshot = quizDoc.reference.collection(quizCollection).get().await()
                    val items = subCollectionSnapshot.documents.mapNotNull { doc ->
                        val type = if (learningPreference == "EN") {
                            doc.getString("tipoPregunta")?.lowercase()?.let {
                                if (it == "blanco") "fillInBlank" else "multipleChoice"
                            } ?: "multipleChoice"
                        } else {
                            doc.getString("questionType") ?: "multipleChoice"
                        }

                        val question = if (learningPreference == "EN") doc.getString("pregunta") else doc.getString("question")
                        val options = (doc.get("options") ?: doc.get("opciones")) as? List<*>
                        val correctAnswer = doc.getString("respuesta") ?: doc.getString("answer")

                        if (question != null && correctAnswer != null) {
                            QuizQuestion(question, options?.filterIsInstance<String>() ?: emptyList(), correctAnswer, type)
                        } else null
                    }
                    allQuestions.addAll(items)
                }

                allQuestions.shuffle()
                quizQuestions.value = allQuestions
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }
}
fun calculateScore(questions: List<QuizQuestion>, selectedAnswers: Map<Int, String>): Int {
    var score = 0
    for ((index, selectedAnswer) in selectedAnswers) {
        val correctAnswer = questions.getOrNull(index)?.answer
        if (normalize(selectedAnswer) == normalize(correctAnswer ?: "")) {
            score++
        }
    }
    return score
}

fun normalize(text: String): String {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()
}

fun saveWrongAnswerToFirestore(question: QuizQuestion) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val wrongAnswer = mapOf(
        "question" to question.question,
        "answer" to question.answer,
    )

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .collection("errorTrack")
        .add(wrongAnswer)
}