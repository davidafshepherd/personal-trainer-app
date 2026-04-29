package com.example.personaltrainerapp.data.workouts

import java.time.LocalDateTime

data class Workout(
    val id: String = "",
    val trainerId: String = "",
    val traineeId: String = "",
    val name: String = "",
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime = LocalDateTime.now(),
    val exercises: List<Exercise> = emptyList()
)

data class Exercise(
    val id: String = "",
    val name: String = "",
    val muscleGroup: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val estimatedMinutes: Int = 0,
    val isComplete: Boolean = false
)