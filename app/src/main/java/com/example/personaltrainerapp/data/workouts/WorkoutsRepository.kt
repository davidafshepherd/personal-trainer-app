package com.example.personaltrainerapp.data.workouts

import com.example.personaltrainerapp.data.calendar.AvailabilitySlot

interface WorkoutsRepository {
    suspend fun getUpcomingWorkouts(
        traineeId: String
    ): List<Workout>

    suspend fun markExerciseComplete(
        workoutId: String,
        exerciseId: String,
        completed: Boolean
    )

    suspend fun getAllWorkouts(): List<Workout>
    suspend fun getWorkoutById(workoutId: String): Workout?
    suspend fun scheduleWorkout(workout: Workout, traineeId: String, slot: AvailabilitySlot)
    suspend fun saveNewWorkout(workout: Workout)
    suspend fun getTrainersWorkouts(trainerId: String): List<Workout>
    suspend fun decoupleWorkout(workoutId: String)
}