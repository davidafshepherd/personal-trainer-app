package com.example.personaltrainerapp.ui.workouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personaltrainerapp.ui.common.AppHeader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TraineeWorkout(
    val id: String,
    val trainerId: String,
    val traineeId: String,
    val name: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val exercises: List<TraineeExercise>
)

data class TraineeExercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val reps: String,
    val estimatedMinutes: Int,
    val isComplete: Boolean = false
)

@Composable
fun WorkoutsScreen() {
    var selectedWorkout by remember { mutableStateOf<TraineeWorkout?>(null) }

    var workouts by remember {
        mutableStateOf(
            listOf(
                sampleWorkout("1", "Workout 1", LocalDateTime.of(2026, 5, 10, 14, 0)),
                sampleWorkout("2", "Workout 2", LocalDateTime.of(2026, 5, 11, 13, 0)),
                sampleWorkout("3", "Workout 3", LocalDateTime.of(2026, 5, 12, 13, 0)),
                sampleWorkout("4", "Workout 4", LocalDateTime.of(2026, 5, 14, 15, 0)),
                sampleWorkout("5", "Workout 5", LocalDateTime.of(2026, 5, 18, 14, 0)),
                sampleWorkout("6", "Workout 6", LocalDateTime.of(2026, 5, 21, 12, 0))
            )
        )
    }

    if (selectedWorkout == null) {
        WorkoutListScreen(
            workouts = workouts,
            onWorkoutClick = { selectedWorkout = it }
        )
    } else {
        WorkoutDetailScreen(
            workout = selectedWorkout!!,
            onBack = { selectedWorkout = null },
            onExerciseToggle = { exerciseId ->
                workouts = workouts.map { workout ->
                    if (workout.id != selectedWorkout!!.id) {
                        workout
                    } else {
                        val updatedWorkout = workout.copy(
                            exercises = workout.exercises.map { exercise ->
                                if (exercise.id == exerciseId) {
                                    exercise.copy(isComplete = !exercise.isComplete)
                                } else {
                                    exercise
                                }
                            }
                        )

                        selectedWorkout = updatedWorkout
                        updatedWorkout
                    }
                }
            }
        )
    }
}

@Composable
private fun WorkoutListScreen(
    workouts: List<TraineeWorkout>,
    onWorkoutClick: (TraineeWorkout) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = "Upcoming Workouts",
            onBack = {}
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(workouts.sortedBy { it.startTime }) { workout ->
                WorkoutListItem(
                    workout = workout,
                    onClick = { onWorkoutClick(workout) }
                )
            }
        }
    }
}

@Composable
private fun WorkoutListItem(
    workout: TraineeWorkout,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = workout.name,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = workout.startTime.format(
                    DateTimeFormatter.ofPattern("d/M/yy · h:mm a")
                )
            )
        }
    }
}

@Composable
private fun WorkoutDetailScreen(
    workout: TraineeWorkout,
    onBack: () -> Unit,
    onExerciseToggle: (String) -> Unit
) {
    val now = LocalDateTime.now()
    val canCompleteExercises = now >= workout.startTime && now <= workout.endTime

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("←")
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = workout.name,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Estimated ${estimatedWorkoutMinutes(workout)} min"
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!canCompleteExercises) {
            Text(
                text = "Exercises can only be marked complete during the allotted workout time.",
                modifier = Modifier.alpha(0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(workout.exercises) { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    canComplete = canCompleteExercises,
                    onToggle = { onExerciseToggle(exercise.id) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: TraineeExercise,
    canComplete: Boolean,
    onToggle: () -> Unit
) {
    val itemAlpha = if (canComplete) 1f else 0.45f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${exercise.name} (${exercise.muscleGroup})",
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${exercise.reps} reps (${exercise.estimatedMinutes} min)"
            )
        }

        Checkbox(
            checked = exercise.isComplete,
            onCheckedChange = {
                if (canComplete) {
                    onToggle()
                }
            },
            enabled = canComplete
        )
    }
}

private fun sampleWorkout(
    id: String,
    name: String,
    startTime: LocalDateTime
): TraineeWorkout {
    return TraineeWorkout(
        id = id,
        trainerId = "trainer-1",
        traineeId = "demo-trainee",
        name = name,
        startTime = startTime,
        endTime = startTime.plusMinutes(60),
        exercises = listOf(
            TraineeExercise("1", "Exercise 1", "Core", "4x10", 10),
            TraineeExercise("2", "Exercise 2", "Legs", "3x12", 8),
            TraineeExercise("3", "Exercise 3", "Chest", "4x8", 10),
            TraineeExercise("4", "Exercise 4", "Back", "3x10", 12),
            TraineeExercise("5", "Exercise 5", "Core", "4x10", 10),
            TraineeExercise("6", "Exercise 6", "Shoulders", "3x10", 10)
        )
    )
}

private fun estimatedWorkoutMinutes(workout: TraineeWorkout): Int {
    return workout.exercises.sumOf { it.estimatedMinutes }
}