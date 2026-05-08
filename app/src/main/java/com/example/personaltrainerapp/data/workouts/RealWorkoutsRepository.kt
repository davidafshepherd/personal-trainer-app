package com.example.personaltrainerapp.data.workouts

import android.util.Log
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.user.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import kotlin.math.roundToInt

class RealWorkoutsRepository : WorkoutsRepository {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun getAllWorkouts(trainerId: String): List<Workout> {
        return try {
            val snapshot = db.collection("workouts")
                //.whereEqualTo("trainerId", trainerId)
                .get()
                .await()
            Log.d("Repository", "" + snapshot.map { it.id })

            snapshot.documents.forEach {
                val obj = it.toObject(Workout::class.java)

                Log.d(
                    "Firestore",
                    "id=${it.id}, success=${obj != null}, data=${it.data}"
                )
            }
            snapshot.documents.map { doc ->
                doc.toObject(Workout::class.java)

            }
        } catch (e: Exception) {
            emptyList()
        } as List<Workout>
    }

    override suspend fun getWorkoutById(workoutId: String): Workout? {
        return try {
            val doc = db.collection("workouts")
                .document(workoutId)
                .get()
                .await()

            doc.toObject(Workout::class.java)

        } catch (e: Exception) {
            null
        }
    }

    suspend fun setWorkoutById(workoutId: String, workout: Workout) {
        return try {
            db.collection("workouts").document(workoutId).set(workout).await()
        } catch (e: Exception) {

        } as Unit

    }

    override suspend fun getUpcomingWorkouts(
        traineeId: String
    ): List<Workout> {
        return try {
            val snapshot = db.collection("workouts")
                .whereEqualTo("traineeId", traineeId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Workout::class.java)
            }.sortedBy { it.startTime }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun markExerciseComplete(
        workoutId: String,
        exerciseId: String,
        completed: Boolean
    ) {

        val workout = getWorkoutById(workoutId)
        setWorkoutById(
            workoutId, workout!!.copy(
                exercises = workout.exercises.map { exercise ->
                    if (exercise.id == exerciseId) {
                        exercise.copy(completed = completed)
                    } else {
                        exercise
                    }
                }
            ))
    }


    override suspend fun scheduleWorkout(
        workout: Workout,
        traineeId: String,
        slot: AvailabilitySlot
    ) {
        setWorkoutById(
            workout.id, workout.copy(
                traineeId = traineeId,
                startTime = Timestamp.now(),
                endTime = Timestamp(
                    Date.from(
                        Instant.now().plus(Duration.ofMinutes(workout.estimatedMinutes.toLong()))
                    )
                )
            )
        )
    }

    override suspend fun saveNewWorkout(workout: Workout) {
        val doc = db.collection("workouts").document()
        doc.set(workout.copy(id = doc.id))
    }
}