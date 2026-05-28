package com.example.personaltrainerapp.ui.workouts

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personaltrainerapp.data.sensors.ExerciseMotionTracker
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.ui.common.AppHeader
import com.google.firebase.Timestamp
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(title = "Upcoming Workouts")

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> Text("Loading workouts...")
            error != null -> Text(error)
            workouts.isEmpty() -> Text("No upcoming workouts")

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(workouts.sortedBy { it.startTime.toDate().time }) { workout ->
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

            Text(text = workout.startTime.formatWorkoutTime())
        }
    }
}

@Composable
private fun WorkoutDetailScreen(
    workout: Workout,
    onBack: () -> Unit,
    onExerciseToggle: (String) -> Unit
) {
    val canCompleteExercises = isNowWithinWorkoutTime(workout)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

        Spacer(modifier = Modifier.height(12.dp))

        if (workout.description.isNotBlank()) {
            Text(
                text = workout.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        WorkoutTypeBreakdown(workout = workout)

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
private fun WorkoutTypeBreakdown(
    workout: Workout
) {
    if (workout.exerciseTypeSplit.isEmpty()) return

    val total = workout.exerciseTypeSplit.values.sum().takeIf { it > 0 } ?: return

    Column {
        Text(
            text = "Workout breakdown",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        workout.exerciseTypeSplit.forEach { (type, amount) ->
            val fraction = amount.toFloat() / total.toFloat()
            val percentage = (fraction * 100).toInt()

            Column(
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row {
                    Text(
                        text = type,
                        modifier = Modifier.weight(1f)
                    )

                    Text("$percentage%")
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth()
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
    val context = LocalContext.current
    val itemAlpha = if (canComplete) 1f else 0.45f

    var isTracking by remember(exercise.id) { mutableStateOf(false) }
    var sensorUnavailable by remember(exercise.id) { mutableStateOf(false) }
    var completedTrackedReps by remember(exercise.id) { mutableIntStateOf(0) }
    var requiredTrackedReps by remember(exercise.id) { mutableIntStateOf(exercise.reps) }
    var currentRepMovementRatio by remember(exercise.id) { mutableStateOf(0f) }

    val estimatedMillisPerRep = maxOf(
        1L,
        exercise.estimatedMinutes * 60_000L
    )

    val tracker = remember(exercise.id) {
        ExerciseMotionTracker(
            context = context.applicationContext,
            requiredReps = exercise.reps,
            estimatedMillisPerRep = estimatedMillisPerRep,
            debugTimeScale = 0.05f,
            requiredMovementRatio = 0.5f,
            onProgressChanged = { completed, required, ratio ->
                completedTrackedReps = completed
                requiredTrackedReps = required
                currentRepMovementRatio = ratio
            },
            onCompleted = {
                isTracking = false

                if (!exercise.completed && canComplete) {
                    onToggle()
                }
            }
        )
    }

    DisposableEffect(exercise.id) {
        onDispose {
            tracker.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
    ) {
        Row(
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
                    text = exercise.name,
                    fontWeight = FontWeight.SemiBold
                )

                if (exercise.muscleGroups.isNotEmpty()) {
                    Text(text = exercise.muscleGroups.joinToString(", "))
                }

                Text(
                    text = "${exercise.reps} reps (${exercise.estimatedMinutes * exercise.reps} min)"
                )
            }

            Checkbox(
                checked = exercise.completed,
                onCheckedChange = {
                    if (canComplete) {
                        tracker.stop()
                        isTracking = false
                        onToggle()
                    }
                },
                enabled = canComplete
            )
        }

        if (canComplete && !exercise.completed) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTracking) {
                    OutlinedButton(
                        onClick = {
                            tracker.stop()
                            isTracking = false
                        }
                    ) {
                        Text("Stop tracking")
                    }
                } else {
                    Button(
                        onClick = {
                            sensorUnavailable = false
                            currentRepMovementRatio = 0f

                            val started = tracker.start()

                            if (started) {
                                isTracking = true
                            } else {
                                sensorUnavailable = true
                            }
                        }
                    ) {
                        Text("Start sensor tracking")
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isTracking) {
                    Text("$completedTrackedReps/$requiredTrackedReps reps")
                }
            }

            if (isTracking) {
                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { currentRepMovementRatio },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Current rep movement: ${(currentRepMovementRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (sensorUnavailable) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Accelerometer unavailable. Use the checkbox instead.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun estimatedWorkoutMinutes(workout: Workout): Int {
    return if (workout.estimatedMinutes > 0) {
        workout.estimatedMinutes
    } else {
        workout.exercises.sumOf { it.estimatedMinutes }
    }
}

private fun isNowWithinWorkoutTime(workout: Workout): Boolean {
    val now = Timestamp.now().toDate().time
    val start = workout.startTime.toDate().time
    val end = workout.endTime.toDate().time

    return now in start..end
}

private fun Timestamp.formatWorkoutTime(): String {
    return this.toDate()
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("d/M/yy · h:mm a"))
}