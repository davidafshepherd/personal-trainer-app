package com.example.personaltrainerapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.example.personaltrainerapp.data.user.UserRole

data class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: NavKey
)

fun topLevelDestinationsFor(role: UserRole): List<TopLevelDestination> {
    return when (role) {
        UserRole.TRAINEE -> listOf(
            TopLevelDestination("Home", Icons.Default.Home, TraineeHomeRoute),
            TopLevelDestination("Calendar", Icons.Default.CalendarMonth, TraineeCalendarRoute),
            TopLevelDestination("Workouts", Icons.Default.FitnessCenter, TraineeWorkoutsRoute),
            TopLevelDestination("Trainer", Icons.Default.Group, TraineeTrainerRoute),
            TopLevelDestination("Profile", Icons.Default.Person, TraineeProfileRoute)
        )

        UserRole.TRAINER -> listOf(
            TopLevelDestination("Home", Icons.Default.Home, TrainerHomeRoute),
            TopLevelDestination("Trainees", Icons.Default.Group, TrainerTraineesRoute),
            TopLevelDestination("Library", Icons.Default.LibraryBooks, TrainerLibraryRoute),
            TopLevelDestination("Profile", Icons.Default.Person, TrainerProfileRoute)
        )
    }
}
