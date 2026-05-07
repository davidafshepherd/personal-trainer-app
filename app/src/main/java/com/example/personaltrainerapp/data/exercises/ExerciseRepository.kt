package com.example.personaltrainerapp.data.exercises

import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.workouts.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseRepository {

    private val db = FirebaseFirestore.getInstance()
    suspend fun getAllExercises(): List<Exercise> {
        return try {
            val snapshot = db.collection("exercises")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Exercise::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}