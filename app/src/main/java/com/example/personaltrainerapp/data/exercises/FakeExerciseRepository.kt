package com.example.personaltrainerapp.data.exercises

import com.example.personaltrainerapp.data.workouts.Exercise

class FakeExerciseRepository {
    private var exercises = listOf(
        Exercise("1", "Exercise 1", muscleGroups = listOf("Core","Legs"),reps = 10, estimatedMinutes = 10),
        Exercise("2", "Exercise 2", muscleGroups = listOf("Legs","Chest"),reps = 12, estimatedMinutes = 8),
        Exercise("3", "Exercise 3", muscleGroups = listOf("Chest","Back"),reps = 8, estimatedMinutes = 10),
        Exercise("4", "Exercise 4", muscleGroups = listOf("Back","Core"),reps = 10, estimatedMinutes = 12),
        Exercise("5", "Exercise 5", muscleGroups = listOf("Core","Shoulders"),reps = 10, estimatedMinutes = 10),
        Exercise("6", "Exercise 6", muscleGroups = listOf("Shoulders","Core"),reps = 10, estimatedMinutes = 10))

    suspend fun getAllExercises(): List<Exercise> {
        return exercises
    }


}