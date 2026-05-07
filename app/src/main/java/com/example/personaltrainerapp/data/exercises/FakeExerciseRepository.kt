package com.example.personaltrainerapp.data.exercises

import com.example.personaltrainerapp.data.workouts.Exercise

class FakeExerciseRepository {
    private var exercises = listOf(
        Exercise("1", "Exercise 1", muscleGroup = "Core", sets = 4, reps = 10, estimatedMinutes = 10),
        Exercise("2", "Exercise 2", muscleGroup = "Legs", sets = 3, reps = 12, estimatedMinutes = 8),
        Exercise("3", "Exercise 3", muscleGroup = "Chest", sets = 4, reps = 8, estimatedMinutes = 10),
        Exercise("4", "Exercise 4", muscleGroup = "Back", sets = 3, reps = 10, estimatedMinutes = 12),
        Exercise("5", "Exercise 5", muscleGroup = "Core", sets = 4, reps = 10, estimatedMinutes = 10),
        Exercise("6", "Exercise 6", muscleGroup = "Shoulders", sets = 3, reps = 10, estimatedMinutes = 10))

    suspend fun getAllExercises(): List<Exercise> {
        return exercises
    }


}