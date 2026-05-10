package com.example.personaltrainerapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.personaltrainerapp.ui.trainees.TraineeViewModel
import com.example.personaltrainerapp.ui.workouts.WorkoutsViewModel
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Home screen for signed-in trainers.
 */
@Composable
fun TrainerHomeScreen(
    user: UserProfile,
    onManageTraineesClick: () -> Unit = {},
    onViewLibraryClick: () -> Unit = {}
) {
    // ViewModels for trainee roster and scheduled workouts
    val traineeViewModel = viewModel<TraineeViewModel>()
    val traineeUiState by traineeViewModel.uiState.collectAsState()

    val workoutsViewModel = viewModel<WorkoutsViewModel>()
    val workoutsUiState by workoutsViewModel.uiState.collectAsState()

    // Load trainees and all workouts when the screen first enters composition
    LaunchedEffect(user.uid) {
        traineeViewModel.loadTrainees(user.uid)
        workoutsViewModel.loadAllWorkouts()
    }

    // Filter to only workouts that belong to this trainer
    val myWorkouts = workoutsUiState.workouts.filter { it.trainerId == user.uid }

    // Earliest upcoming session across all the trainer's trainees
    val nextSession = myWorkouts
        .filter { it.startTime.toDate().time >= Timestamp.now().toDate().time }
        .minByOrNull { it.startTime.toDate().time }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // AppHeader
        AppHeader(title = "Home")

        Spacer(Modifier.height(16.dp))

        // Personalised greeting
        Text(
            text = "Welcome back, ${user.displayName.ifBlank { "Trainer" }}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        // Trainees card
        TrainerHomeCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Trainees icon
                Icon(Icons.Default.Groups, contentDescription = null)

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Trainees", fontWeight = FontWeight.SemiBold)

                    // Number of active trainees
                    val count = traineeUiState.myTrainees.size
                    if (count == 0) {
                        Text("You have no trainees yet.")
                    } else {
                        Text("$count active trainee${if (count == 1) "" else "s"}")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Manage Trainees button
            Button(
                onClick = onManageTraineesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Trainees")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Next session card
        TrainerHomeCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Session icon
                Icon(Icons.Default.FitnessCenter, contentDescription = null)

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Next Session", fontWeight = FontWeight.SemiBold)

                    // Next session
                    if (nextSession == null) {
                        Text("No upcoming sessions scheduled.")
                    } else {
                        Text(nextSession.name)
                        Text(nextSession.startTime.formatDateTime())
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Overview card
        TrainerHomeCard {
            Text("Overview", fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(8.dp))

            // Total number of trainees
            Text("Total trainees: ${traineeUiState.myTrainees.size}")

            // Total number of upcoming sessions
            val scheduledCount = myWorkouts
                .count { Timestamp.now().toDate().time <= it.startTime.toDate().time }
            Text("Upcoming sessions: $scheduledCount")

            // Total number of completed workouts
            val completedCount = myWorkouts.count { workout ->
                workout.exercises.isNotEmpty() && workout.exercises.all { it.completed }
            }
            Text("Completed workouts: $completedCount")
        }

        Spacer(Modifier.height(12.dp))

        // Library card
        TrainerHomeCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Library icon
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)

                Spacer(Modifier.width(12.dp))

                // Card info
                Column(modifier = Modifier.weight(1f)) {
                    Text("Workout Library", fontWeight = FontWeight.SemiBold)
                    Text("Build and manage your workout templates.")
                }
            }

            Spacer(Modifier.height(12.dp))

            // View Library button
            Button(
                onClick = onViewLibraryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Library")
            }
        }
    }
}

/**
 * Card used by each section of the trainer home screen.
 */
@Composable
private fun TrainerHomeCard(
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

/**
 * Formats a Firebase Timestamp as a human-readable date and time string, e.g. "9/5/26 · 3:00 PM".
 */
private fun Timestamp.formatDateTime(): String {
    return this.toDate()
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("d/M/yy · h:mm a"))
}
