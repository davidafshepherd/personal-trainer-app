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
fun AppFrame(
    user: UserProfile,
    modifier: Modifier = Modifier
) {
    val destinations = topLevelDestinationsFor(user.role)

    val startRoute = when (user.role) {
        UserRole.TRAINEE -> TraineeHomeRoute
        UserRole.TRAINER -> TrainerHomeRoute
    }

    val navState = rememberNavigationState(
        startRoute = startRoute,
        topLevelRoutes = destinations.map { it.route }.toSet()
    )

    val navigator = remember(navState) {
        Navigator(navState)
    }

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
        NavDisplay(
            entries = navState.toDecoratedEntries(entryProvider),
            onBack = { navigator.goBack() }
        )
    }
}
