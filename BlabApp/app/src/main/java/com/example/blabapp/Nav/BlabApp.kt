package com.example.blabapp.Nav

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class BlabApp : Application() {
    companion object{
        lateinit var accountRepository: AccountRepository
    }

    override fun onCreate() {
        super.onCreate()

        lateinit var firestoreDB: FirebaseFirestore

        runBlocking(Dispatchers.IO) {
            firestoreDB = FirebaseFirestore.getInstance()
            accountRepository = AccountRepository(firestoreDB)
        }
    }
}