package com.example.personaltrainerapp.data.exercises

import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.workouts.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExerciseRepository {

    private val db = FirebaseFirestore.getInstance()
    suspend fun getAllExercises(trainerId: String): List<Exercise> {
        return try {
            val snapshot = db.collection("exercises")
                .whereEqualTo("trainerId", trainerId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Exercise::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveNewExercise(exercise: Exercise){
        val doc = db.collection("exercises").document()
        doc.set(exercise.copy(id = doc.id))
    }
}