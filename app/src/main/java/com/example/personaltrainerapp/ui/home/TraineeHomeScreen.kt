package com.example.personaltrainerapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.ui.common.AppHeader
import com.example.personaltrainerapp.ui.trainer.TrainerViewModel
import com.example.personaltrainerapp.ui.workouts.WorkoutsViewModel
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TraineeHomeScreen(
    user: UserProfile,
    onFindTrainerClick: () -> Unit = {},
    onViewWorkoutsClick: () -> Unit = {},
    onUpdateAvailabilityClick: () -> Unit = {}
) {
    val trainerViewModel = viewModel<TrainerViewModel>()
    val trainerUiState by trainerViewModel.uiState.collectAsState()

    val workoutsViewModel = viewModel<WorkoutsViewModel>()
    val workoutsUiState by workoutsViewModel.uiState.collectAsState()

    LaunchedEffect(user.uid) {
        trainerViewModel.loadAssignedTrainer(user.uid)
        workoutsViewModel.loadWorkouts(user.uid)
    }

    val nextWorkout = workoutsUiState.workouts
        .filter { it.endTime.toDate().time >= Timestamp.now().toDate().time }
        .minByOrNull { it.startTime.toDate().time }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppHeader(title = "Home")

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Welcome back, ${user.displayName.ifBlank { "Trainee" }}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        HomeCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null)
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Trainer", fontWeight = FontWeight.SemiBold)

                    if (trainerUiState.selectedTrainer == null) {
                        Text("You do not have a trainer yet.")
                    } else {
                        Text(trainerUiState.selectedTrainer!!.displayName)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (trainerUiState.selectedTrainer == null) {
                Button(
                    onClick = onFindTrainerClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Find a Trainer")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        HomeCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Next Workout", fontWeight = FontWeight.SemiBold)

                    if (nextWorkout == null) {
                        Text("No upcoming workouts scheduled.")
                    } else {
                        Text(nextWorkout.name)
                        Text(nextWorkout.startTime.formatDateTime())
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onViewWorkoutsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Workouts")
            }
        }

        Spacer(Modifier.height(12.dp))

        HomeCard {
            Text("Progress", fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(8.dp))

            Text("Completed workouts: ${workoutsUiState.workouts.count { workout ->
                workout.exercises.isNotEmpty() && workout.exercises.all { it.completed }
            }}")

            Text("Total assigned workouts: ${workoutsUiState.workouts.size}")
        }

        Spacer(Modifier.height(12.dp))

        HomeCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Availability", fontWeight = FontWeight.SemiBold)
                    Text("Keep your trainer updated with when you are free.")
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onUpdateAvailabilityClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Availability")
            }
        }
    }
}

@Composable
private fun HomeCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

private fun Timestamp.formatDateTime(): String {
    return this.toDate()
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("d/M/yy · h:mm a"))
}