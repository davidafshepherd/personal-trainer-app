package com.example.personaltrainerapp.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.example.personaltrainerapp.navigation.*
import com.example.personaltrainerapp.ui.calendar.CalendarScreen
import com.example.personaltrainerapp.ui.library.LibraryScreen
import com.example.personaltrainerapp.ui.trainees.TraineesScreen
import com.example.personaltrainerapp.ui.trainer.TrainerScreen
import com.example.personaltrainerapp.ui.workouts.WorkoutsScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith

@Composable
fun AppFrame(user: UserProfile, modifier: Modifier = Modifier) {
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
        entry<TraineeCalendarRoute> { CalendarScreen(traineeId = user.uid) }
        entry<TraineeWorkoutsRoute> { WorkoutsScreen(traineeId = user.uid) }
        entry<TraineeTrainerRoute> { TrainerScreen(traineeId = user.uid) }
        entry<TraineeProfileRoute> { ProfileScreen(role = UserRole.TRAINEE) }

        entry<TrainerHomeRoute> { BlankScreen("Home") }
        entry<TrainerTraineesRoute> { TraineesScreen() }
        entry<TrainerLibraryRoute> { LibraryScreen() }
        entry<TrainerProfileRoute> { ProfileScreen(role = UserRole.TRAINER) }
    }

    NavigationSuiteScaffold(
        modifier = modifier.fillMaxSize(),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            NavDisplay(
                entries = navState.toDecoratedEntries(entryProvider),
                onBack = { navigator.goBack() },
                transitionSpec = {
                    EnterTransition.None togetherWith ExitTransition.None
                },
                popTransitionSpec = {
                    EnterTransition.None togetherWith ExitTransition.None
                },
                predictivePopTransitionSpec = {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            )
        }
    }
}