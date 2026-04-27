package com.example.personaltrainerapp.ui.app

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import com.example.personaltrainerapp.data.UserProfile
import com.example.personaltrainerapp.data.UserRole
import com.example.personaltrainerapp.navigation.*

@Composable
fun AppFrame(user: UserProfile, modifier: Modifier = Modifier) {
    // Top level destinations
    val destinations = topLevelDestinationsFor(user.role)

    // Start route
    val startRoute = when (user.role) {
        UserRole.TRAINEE -> TraineeHomeRoute
        UserRole.TRAINER -> TrainerHomeRoute
    }

    // Navigation state
    val navState = rememberNavigationState(
        startRoute = startRoute,
        topLevelRoutes = destinations.map { it.route }.toSet()
    )

    // Navigator
    val navigator = remember(navState) {
        Navigator(navState)
    }

    // Entry provider
    val entryProvider = entryProvider {
        entry<TraineeHomeRoute> { BlankScreen("Home") }
        entry<TraineeCalendarRoute> { BlankScreen("Calendar") }
        entry<TraineeWorkoutsRoute> { BlankScreen("Workouts") }
        entry<TraineeTrainerRoute> { BlankScreen("Trainer") }
        entry<TraineeProfileRoute> { ProfileScreen(role = UserRole.TRAINEE) }

        entry<TrainerHomeRoute> { BlankScreen("Home") }
        entry<TrainerTraineesRoute> { BlankScreen("Trainees") }
        entry<TrainerLibraryRoute> { BlankScreen("Library") }
        entry<TrainerProfileRoute> { ProfileScreen(role = UserRole.TRAINER) }
    }

    // Navigation suite scaffold
    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            destinations.forEach { destination ->
                item(
                    selected = navState.topLevelRoute == destination.route,
                    onClick = { navigator.navigate(destination.route) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) }
                )
            }
        }
    ) {
        // Navigation display
        NavDisplay(
            entries = navState.toDecoratedEntries(entryProvider),
            onBack = { navigator.goBack() }
        )
    }
}
