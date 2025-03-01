package com.example.blabapp.Nav

data class User(
    val name: String="NO NAME",
    var email: String = "EMPTY@EMAIL.COM",
    val chatList: MutableList<String> = mutableListOf<String>(),
    val friendList: MutableList<String> = mutableListOf<String>(),
    var rank: Int = 0,
    var userBio: String = "NO BIO",
    val userId: String = "NO ID"
) {

}