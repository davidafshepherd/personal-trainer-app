package com.example.personaltrainerapp.data.exercises

import com.example.personaltrainerapp.data.workouts.Exercise

class FakeExerciseRepository : ExerciseRepository{
    private var exercises = listOf(
        Exercise("1", "Exercise 1", "Core", sets = 4, reps = 10, estimatedMinutes = 10),
        Exercise("2", "Exercise 2", "Legs", sets = 3, reps = 12, estimatedMinutes = 8),
        Exercise("3", "Exercise 3", "Chest", sets = 4, reps = 8, estimatedMinutes = 10),
        Exercise("4", "Exercise 4", "Back", sets = 3, reps = 10, estimatedMinutes = 12),
        Exercise("5", "Exercise 5", "Core", sets = 4, reps = 10, estimatedMinutes = 10),
        Exercise("6", "Exercise 6", "Shoulders", sets = 3, reps = 10, estimatedMinutes = 10))

    override suspend fun getAllExercises(): List<Exercise> {
        return exercises
    }

}