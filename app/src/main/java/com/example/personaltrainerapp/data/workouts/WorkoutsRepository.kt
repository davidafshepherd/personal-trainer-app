package com.example.personaltrainerapp.data.workouts

interface WorkoutsRepository {
    suspend fun getUpcomingWorkouts(
        traineeId: String
    ): List<Workout>

    suspend fun markExerciseComplete(
        workoutId: String,
        exerciseId: String,
        isComplete: Boolean
    )

    suspend fun getAllWorkouts(): List<Workout>
    suspend fun getWorkoutById(workoutId: String): Workout?
}