package com.example.personaltrainerapp.data.exercises

import com.example.personaltrainerapp.data.workouts.Exercise

interface ExerciseRepository {
    suspend fun getAllExercises(): List<Exercise>
}