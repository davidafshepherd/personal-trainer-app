package com.example.personaltrainerapp.data.trainer

import android.util.Log
import com.example.personaltrainerapp.data.user.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TrainerRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getAllTrainers(): List<UserProfile> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", "TRAINER")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserProfile::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrainerById(trainerId: String): UserProfile? {
        return try {
            val doc = db.collection("users")
                .document(trainerId)
                .get()
                .await()

            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun requestTrainer(
        traineeId: String,
        trainerId: String
    ) {
        try {
            val requestId = "${traineeId}_$trainerId"

            db.collection("trainerRequests")
                .document(requestId)
                .set(
                    TrainerRequest(
                        id = requestId,
                        traineeId = traineeId,
                        trainerId = trainerId,
                        status = "PENDING",
                        createdAt = System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e("TrainerRepository", "Failed to request trainer", e)
        }
    }
}