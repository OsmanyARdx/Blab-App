package com.example.blabapp.Nav

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val answer: String,
    val questionType: String = ""
)