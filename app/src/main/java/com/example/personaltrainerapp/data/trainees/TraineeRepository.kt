package com.example.personaltrainerapp.data.trainees

import android.util.Log
import com.example.personaltrainerapp.data.trainer.TrainerRequest
import com.example.personaltrainerapp.data.user.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    suspend fun updateTrainer(
        traineeId: String,
        trainerId: String
    ) {
        db.collection("users")
            .document(traineeId)
            .update("trainerId", trainerId)
            .await()
    }

    suspend fun getRequests(trainerId: String): List<TrainerRequest> {
        return try {
            val snapshot = db.collection("trainerRequests")
                .whereEqualTo("trainerId", trainerId)
                .whereEqualTo("status", "PENDING")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(TrainerRequest::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setTrainerAvailability(trainerId: String, availability: Int): Boolean {
        return try {
            db.collection("users")
                .document(trainerId)
                .update("availability", availability)
                .await()
            true
        } catch (e: Exception) {
            Log.e("TrainerRepository", "Failed to set Trainer Availability", e)
            false
        }
    }

    suspend fun getTrainerAvailability(trainerId: String): Int {
        return try {
            val doc = db.collection("users")
                .document(trainerId)
                .get()
                .await()

            doc.toObject(UserProfile::class.java)!!.availability
        } catch (e: Exception) {
            Log.e("TraineeRepository", "Failed to get Trainer Availability")
            0
        }
    }

    fun updateRequestStatus(requestId: String, status: String) {
        try {
            db.collection("trainerRequests").document(requestId).set(
                mapOf("status" to status),
                SetOptions.merge()
            )
        } catch (e: Exception) {
            Log.e("TraineeRepository", "Failed to update trainer request")

        }
    }
}