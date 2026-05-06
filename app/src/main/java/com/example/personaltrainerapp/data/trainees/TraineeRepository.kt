package com.example.personaltrainerapp.data.trainees

import com.example.personaltrainerapp.data.user.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TraineeRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getAllTrainees(): List<UserProfile> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", "TRAINEE")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserProfile::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTraineeById(traineeId: String): UserProfile? {
        return try {
            val doc = db.collection("users")
                .document(traineeId)
                .get()
                .await()

            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addTrainee(
        trainerId: String,
        traineeId: String
    ) {
        db.collection("users")
            .document(trainerId)
            //.update("trainerId", trainerId)
            //.await()
    }
}