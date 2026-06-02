package com.example.personaltrainerapp.data.workouts

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.user.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import kotlin.math.roundToInt

class RealWorkoutsRepository : WorkoutsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    suspend fun uploadMedia(uri: String, mediaId:String): Uri {
        val ref = storage.reference.child("workout_media/$mediaId")
        ref.putFile(uri.toUri()).await()
        return ref.downloadUrl.await()
    }
    override suspend fun getAllWorkouts(): List<Workout> {
        return try {
            val snapshot = db.collection("workouts")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Workout::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrainersWorkouts(trainerId: String): List<Workout> {
        return try {
            val snapshot = db.collection("workouts")
                .whereEqualTo("trainerId", trainerId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Workout::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun decoupleWorkout(workoutId: String) {
        try {
            db.collection("workouts")
                .document(workoutId)
                .update("traineeId", "")
                .await()
        } catch (e: Exception) {
            Log.e("Workout Repository", "Failed to remove workout from trainee")
        }
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
        try {
            db.collection("workouts")
                .document(workoutId)
                .set(workout)
                .await()
        } catch (e: Exception) {
            Log.e("RealWorkoutsRepository", "Failed to set workout $workoutId", e)
        }

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

        if (workout == null) {
            Log.e("RealWorkoutsRepository", "Workout not found: $workoutId")
            return
        }

        val updatedWorkout = workout.copy(
            exercises = workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    exercise.copy(completed = completed)
                } else {
                    exercise
                }
            }
        )

        setWorkoutById(workoutId, updatedWorkout)
    }


    override suspend fun scheduleWorkout(
        workout: Workout,
        traineeId: String,
        slot: AvailabilitySlot
    ) {
        val startTime = Timestamp(
            Date.from(
                slot.date.atTime(slot.hour,
                    0).atZone(ZoneId.systemDefault()).toInstant())
        )
        try {
            setWorkoutById(
                workout.id, workout.copy(
                    traineeId = traineeId,
                    startTime = startTime,
                    endTime = Timestamp(startTime.toInstant().plusSeconds((workout.estimatedMinutes*60).toLong()))
                )
            )
        } catch (e: Exception){
            Log.e("RealWorkoutsRepository", "Unable to schedule workout")
        }
    }

    override suspend fun saveNewWorkout(workout: Workout) {
        val doc = db.collection("workouts").document()
        var newWorkout = workout.copy(id = doc.id)
        for (exercise: Exercise in workout.exercises) {
            if (exercise.uri != ""){
                newWorkout = newWorkout.copy(
                    exercises = workout.exercises.map {
                        if (it.id == exercise.id){
                            it.copy(
                                uri = uploadMedia(exercise.uri, "${doc.id}_${exercise.id}").toString()
                            )
                        }else{
                            it
                        }
                    }
                )
            }
        }
        doc.set(newWorkout)
    }
}