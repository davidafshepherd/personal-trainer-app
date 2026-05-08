package com.example.personaltrainerapp.data.workouts

import com.google.firebase.Timestamp
import java.time.LocalDateTime

data class Workout(
    val id: String = "",
    val trainerId: String = "",
    val traineeId: String = "",
    val name: String = "",
    val description: String = "Placeholder Description of exercise",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val exercises: List<Exercise> = emptyList(),
    val estimatedMinutes: Int = 0,
    val exerciseTypeSplit : Map<String, Int> = emptyMap()
)

data class Exercise(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val muscleGroups: List<String> = emptyList(),
    var reps: Int = 8,
    val estimatedMinutes: Int = 0,
    val completed: Boolean = false,
    val trainerId: String = ""
)