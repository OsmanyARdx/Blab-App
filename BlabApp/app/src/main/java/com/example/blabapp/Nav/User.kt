package com.example.blabapp.Nav

data class User(
    val name: String="NO NAME",
    val userId: String = "NO ID",
    var email: String = "EMPTY@EMAIL.COM",
    val imageUrl: String = "blankimage",
    var userBio: String = "NO BIO",
    val userStreak: String = "NO ID",
    var userRank: String = "0",
    val learning: String = "ES",
    val chatList: MutableList<String> = mutableListOf<String>(),
    val friendList: MutableList<String> = mutableListOf<String>(),




) {

}