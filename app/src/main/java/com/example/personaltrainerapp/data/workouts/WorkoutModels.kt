package com.example.personaltrainerapp.data.workouts

import java.time.LocalDateTime

data class Workout(
    val id: String = "",
    val trainerId: String = "",
    val traineeId: String = "",
    val name: String = "",
    val description: String = "Placeholder Description of exercise",
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime = LocalDateTime.now(),
    val exercises: List<Exercise> = emptyList(),
    val estimatedMinutes: Int = 0,
    val exerciseTypeSplit : List<Pair<String, Int>> = emptyList()
)

data class Exercise(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val muscleGroup: String = "",
    val sets: Int = 0,
    var reps: Int = 8,
    val estimatedMinutes: Int = 0,
    val isComplete: Boolean = false,
    val trainerId: String = ""
)