package com.example.personaltrainerapp.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

class AppNavigationState(startRoute: NavKey, topLevelRoutes: List<NavKey>) {
    var selectedTopLevelRoute : NavKey by mutableStateOf(startRoute)
        private set

    private val backStacks: Map<NavKey, SnapshotStateList<NavKey>> =
        topLevelRoutes.associateWith { route -> mutableStateListOf(route) }

    val currentBackStack: SnapshotStateList<NavKey>
        get() = backStacks.getValue(selectedTopLevelRoute)

    fun switchTopLevelRoute(route: NavKey) {
        selectedTopLevelRoute = route
    }

    fun navigate(route: NavKey) {
        currentBackStack.add(route)
    }

    fun goBack() {
        if (currentBackStack.size > 1) {
            currentBackStack.removeAt(currentBackStack.lastIndex)
        }
    }
}

// Remembers navigation state across recompositions
@Composable
fun rememberAppNavigationState(
    topLevelDestinations: List<TopLevelDestination>,
    startRoute: NavKey = topLevelDestinations.first().route
): AppNavigationState {
    return remember(topLevelDestinations, startRoute) {
        AppNavigationState(
            startRoute = startRoute,
            topLevelRoutes = topLevelDestinations.map { it.route }
        )
    }
}
