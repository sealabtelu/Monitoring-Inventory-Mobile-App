package com.example.inventorymonitoring.data

import android.content.Context
import com.example.inventorymonitoring.data.repository.AuthRepository
import com.example.inventorymonitoring.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(firestore, context)
    }

    val firestoreRepository by lazy { FirestoreRepository(firestore) }
}
