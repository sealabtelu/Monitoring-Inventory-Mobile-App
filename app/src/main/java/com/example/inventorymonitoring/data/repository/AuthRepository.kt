package com.example.inventorymonitoring.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        suspendCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.let { user ->
                        firestore.collection("users")
                            .document(user.uid)
                            .update("terakhir_login", System.currentTimeMillis())

                        continuation.resume(Result.success(user))
                    } ?: continuation.resume(Result.failure(Exception("User not found")))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }

    suspend fun resetPassword(email: String): Result<Unit> =
        suspendCoroutine { continuation ->
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() = auth.signOut()
}