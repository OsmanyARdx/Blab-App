package com.example.blabapp.Nav

import com.google.firebase.Timestamp

data class User(
    val name: String="NO NAME",
    val userId: String = "",
    var email: String = "EMPTY@EMAIL.COM",
    val imageUrl: String = "blankimage",
    var userBio: String = "NO BIO",
    val userStreak: Int = 0,
    var userRank: String = "0",
    val learning: String = "ES",
    val chatList: MutableList<String> = mutableListOf<String>(),
    val friendList: MutableList<String> = mutableListOf<String>(),
    val lastLogin: Timestamp = Timestamp.now(),
    val completeMod: MutableList<String> = mutableListOf<String>(),
    val likedReelsIds: MutableList<String> = mutableListOf<String>(),
    val friendCode: String = "0000000000"




) {

}