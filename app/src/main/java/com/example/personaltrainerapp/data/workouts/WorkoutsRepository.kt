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
}