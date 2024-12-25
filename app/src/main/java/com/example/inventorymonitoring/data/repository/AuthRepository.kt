package com.example.inventorymonitoring.data.repository

import android.content.Context
import com.example.inventorymonitoring.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val firestore: FirebaseFirestore,
    private val context: Context
) {

    private val sharedPreferences by lazy {
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    }

    suspend fun signIn(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userQuery = firestore.collection("user")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (userQuery.documents.isEmpty()) {
                return@withContext Result.failure(Exception("User not found"))
            }

            val userDoc = userQuery.documents.first()
            val user = userDoc.toObject(User::class.java)

            if (user == null) {
                return@withContext Result.failure(Exception("Failed to parse user data"))
            }

            // Check if the provided password matches the stored password
            if (password != user.password) {
                return@withContext Result.failure(Exception("Invalid password"))
            }

            // Store userId in SharedPreferences
            sharedPreferences.edit().putString("userId", user.id).apply()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(email: String, password: String, name: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Check if user already exists
            val existingUser = firestore.collection("user")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!existingUser.documents.isEmpty()) {
                return@withContext Result.failure(Exception("User with this email already exists"))
            }

            // Create new user document
            val newUser = User(
                id = generateUserId(),
                email = email,
                username = name,
                password = password, // Store password as is
            )

            firestore.collection("user").document(newUser.id).set(newUser).await()

            // Store userId in SharedPreferences
            sharedPreferences.edit().putString("userId", newUser.id).apply()

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        val userId = sharedPreferences.getString("userId", null) ?: return@withContext null
        try {
            val userDoc = firestore.collection("user").document(userId).get().await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateUserId(): String {
        return firestore.collection("user").document().id
    }

    fun signOut() {
        sharedPreferences.edit().remove("userId").apply()
    }
}
