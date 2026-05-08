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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.ui.common.AppHeader
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutsScreen(
    traineeId: String = "demo-trainee"
) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<WorkoutsViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(traineeId) {
        viewModel.loadWorkouts(traineeId)
    }

    when {
        uiState.selectedWorkout != null -> {
            WorkoutDetailScreen(
                workout = uiState.selectedWorkout!!,
                onBack = { viewModel.clearSelectedWorkout() },
                onExerciseToggle = { exerciseId ->
                    viewModel.toggleExerciseComplete(
                        workoutId = uiState.selectedWorkout!!.id,
                        exerciseId = exerciseId
                    )
                }
            )
        }

        else -> {
            WorkoutListScreen(
                workouts = uiState.workouts,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onWorkoutClick = { workout ->
                    viewModel.selectWorkout(workout)
                }
            )
        }
    }
}

@Composable
private fun WorkoutListScreen(
    workouts: List<Workout>,
    isLoading: Boolean,
    error: String?,
    onWorkoutClick: (Workout) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = "Upcoming Workouts"
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Text("Loading workouts...")
            }

            error != null -> {
                Text(error)
            }

            workouts.isEmpty() -> {
                Text("No upcoming workouts")
            }

            else -> {
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
    }
}

@Composable
private fun WorkoutListItem(
    workout: Workout,
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
                text = workout.startTime
                    .toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime().format(
                        DateTimeFormatter.ofPattern("d/M/yy · h:mm a")
                    )
            )
        }
    }
}

@Composable
private fun WorkoutDetailScreen(
    workout: Workout,
    onBack: () -> Unit,
    onExerciseToggle: (String) -> Unit
) {
    val now = Timestamp.now()
    val canCompleteExercises = now >= workout.startTime && now <= workout.endTime

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(
            title = workout.name,
            subtitle = "Estimated ${estimatedWorkoutMinutes(workout)} min",
            onBack = onBack,
            trailingContent = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

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
    exercise: Exercise,
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
                text = "${exercise.name} (${exercise.muscleGroups})",
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${exercise.reps} reps (${exercise.estimatedMinutes} min)"
            )
        }

        Checkbox(
            checked = exercise.completed,
            onCheckedChange = {
                if (canComplete) {
                    onToggle()
                }
            },
            enabled = canComplete
        )
    }
}

private fun estimatedWorkoutMinutes(workout: Workout): Int {
    return workout.exercises.sumOf { it.estimatedMinutes }
}