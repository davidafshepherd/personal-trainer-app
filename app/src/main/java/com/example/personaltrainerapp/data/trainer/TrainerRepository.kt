package com.example.personaltrainerapp.data.trainer

import android.util.Log
import com.example.personaltrainerapp.data.user.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    suspend fun getAssignedTrainer(traineeId: String): UserProfile? {
        return try {
            val traineeDoc = db.collection("users")
                .document(traineeId)
                .get()
                .await()

            val trainerId = traineeDoc.getString("trainerId")

            if (trainerId.isNullOrBlank()) {
                null
            } else {
                getTrainerById(trainerId)
            }
        } catch (e: Exception) {
            Log.e("TrainerRepository", "Failed to load assigned trainer", e)
            null
        }
    }

    suspend fun unenrolFromTrainer(traineeId: String) {
        try {
            db.collection("users")
                .document(traineeId)
                .set(
                    mapOf("trainerId" to null),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            Log.e("TrainerRepository", "Failed to unenrol from trainer", e)
        }
    }

    suspend fun hasPendingTrainerRequest(
        traineeId: String,
        trainerId: String
    ): Boolean {
        return try {
            val requestId = "${traineeId}_$trainerId"

            val doc = db.collection("trainerRequests")
                .document(requestId)
                .get()
                .await()

            doc.exists() && doc.getString("status") == "PENDING"
        } catch (e: Exception) {
            Log.e("TrainerRepository", "Failed to check trainer request", e)
            false
        }
    }

    suspend fun setTrainerAvailability(trainerId: String, availability: Int):Boolean {
        return try {
            val doc = db.collection("users")
                .document(trainerId)
                .set(
                    mapOf("availability" to availability),
                    SetOptions.merge()
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e("TrainerRepository", "Failed to set Trainer Availability", e)
            false
        }
    }
}