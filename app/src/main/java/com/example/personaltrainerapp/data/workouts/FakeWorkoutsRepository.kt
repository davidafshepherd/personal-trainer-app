package com.example.personaltrainerapp.data.workouts

import java.time.LocalDateTime

class FakeWorkoutsRepository : WorkoutsRepository {

    private var workouts = listOf(
        sampleWorkout("1", "Workout 1", LocalDateTime.of(2026, 5, 10, 14, 0)),
        sampleWorkout("2", "Workout 2", LocalDateTime.of(2026, 5, 11, 13, 0)),
        sampleWorkout("3", "Workout 3", LocalDateTime.of(2026, 5, 12, 13, 0)),
        sampleWorkout("4", "Workout 4", LocalDateTime.of(2026, 5, 14, 15, 0)),
        sampleWorkout("5", "Workout 5", LocalDateTime.of(2026, 5, 18, 14, 0)),
        sampleWorkout("6", "Workout 6", LocalDateTime.of(2026, 5, 21, 12, 0))
    )

    override suspend fun getAllWorkouts(): List<Workout>{
        return workouts
    }

    override suspend fun getWorkoutById(workoutId:String): Workout?{
        return workouts.find { (id, _, _, _, _, _, _) -> id == workoutId}
    }

    override suspend fun getUpcomingWorkouts(
        traineeId: String
    ): List<Workout> {
        return workouts
            .filter { it.traineeId == traineeId }
            .sortedBy { it.startTime }
    }

    override suspend fun markExerciseComplete(
        workoutId: String,
        exerciseId: String,
        isComplete: Boolean
    ) {
        workouts = workouts.map { workout ->
            if (workout.id != workoutId) {
                workout
            } else {
                workout.copy(
                    exercises = workout.exercises.map { exercise ->
                        if (exercise.id == exerciseId) {
                            exercise.copy(isComplete = isComplete)
                        } else {
                            exercise
                        }
                    }
                )
            }
        }
    }

    private fun sampleWorkout(
        id: String,
        name: String,
        startTime: LocalDateTime
    ): Workout {
        return Workout(
            id = id,
            trainerId = "trainer-1",
            traineeId = "demo-trainee",
            name = name,
            startTime = startTime,
            endTime = startTime.plusMinutes(60),
            exercises = listOf(
                Exercise("1", "Exercise 1", description = "Placeholder Description of exercise", muscleGroup = "Core", sets = 4, reps = 10, estimatedMinutes = 10),
                Exercise("2", "Exercise 2", description = "Placeholder Description of exercise", muscleGroup = "Legs", sets = 3, reps = 12, estimatedMinutes = 8),
                Exercise("3", "Exercise 3", description = "Placeholder Description of exercise", muscleGroup = "Chest", sets = 4, reps = 8, estimatedMinutes = 10),
                Exercise("4", "Exercise 4", description = "Placeholder Description of exercise", muscleGroup = "Back", sets = 3, reps = 10, estimatedMinutes = 12),
                Exercise("5", "Exercise 5", description = "Placeholder Description of exercise", muscleGroup = "Core", sets = 4, reps = 10, estimatedMinutes = 10),
                Exercise("6", "Exercise 6", description = "Placeholder Description of exercise", muscleGroup = "Shoulders", sets = 3, reps = 10, estimatedMinutes = 10)
            )
        )
    }
}