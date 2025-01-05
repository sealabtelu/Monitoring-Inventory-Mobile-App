package com.example.inventorymonitoring.data

import android.annotation.SuppressLint
import android.content.Context
import com.example.inventorymonitoring.data.datasource.FirestoreDataSource
import com.example.inventorymonitoring.data.repository.AuthRepository
import com.example.inventorymonitoring.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ServiceLocator {
    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()
    private val firestoreDataSource = FirestoreDataSource(firestore)
    val firestoreRepository = FirestoreRepository(firestoreDataSource)

    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(firestore, context)
    }
}
