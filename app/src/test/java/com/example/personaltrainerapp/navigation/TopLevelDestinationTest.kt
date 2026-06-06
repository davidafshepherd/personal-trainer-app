package com.example.personaltrainerapp.navigation

import com.example.personaltrainerapp.data.user.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelDestinationTest {

    // ── count ─────────────────────────────────────────────────────────────────

    @Test
    fun `trainee has 5 top-level destinations`() {
        assertEquals(5, topLevelDestinationsFor(UserRole.TRAINEE).size)
    }

    @Test
    fun `trainer has 5 top-level destinations`() {
        assertEquals(5, topLevelDestinationsFor(UserRole.TRAINER).size)
    }

    // ── labels ────────────────────────────────────────────────────────────────

    @Test
    fun `trainee destination labels are Home Calendar Workouts Trainer Profile`() {
        val labels = topLevelDestinationsFor(UserRole.TRAINEE).map { it.label }
        assertEquals(listOf("Home", "Calendar", "Workouts", "Trainer", "Profile"), labels)
    }

    @Test
    fun `trainer destination labels are Home Trainees Library Stats Profile`() {
        val labels = topLevelDestinationsFor(UserRole.TRAINER).map { it.label }
        assertEquals(listOf("Home", "Trainees", "Stats", "Library", "Profile"), labels)
    }

    @Test
    fun `all destination labels are non-blank`() {
        val all = topLevelDestinationsFor(UserRole.TRAINEE) + topLevelDestinationsFor(UserRole.TRAINER)
        assertTrue(all.all { it.label.isNotBlank() })
    }

    // ── routes ────────────────────────────────────────────────────────────────

    @Test
    fun `trainee destination routes are in correct order`() {
        val routes = topLevelDestinationsFor(UserRole.TRAINEE).map { it.route }
        assertEquals(
            listOf(
                TraineeHomeRoute, TraineeCalendarRoute, TraineeWorkoutsRoute,
                TraineeTrainerRoute, TraineeProfileRoute
            ),
            routes
        )
    }

    @Test
    fun `trainer destination routes are in correct order`() {
        val routes = topLevelDestinationsFor(UserRole.TRAINER).map { it.route }
        assertEquals(
            listOf(
                TrainerHomeRoute, TrainerTraineesRoute, TrainerStatsRoute,
                TrainerLibraryRoute, TrainerProfileRoute
            ),
            routes
        )
    }

    @Test
    fun `trainee first destination is TraineeHomeRoute`() {
        assertEquals(TraineeHomeRoute, topLevelDestinationsFor(UserRole.TRAINEE).first().route)
    }

    @Test
    fun `trainer first destination is TrainerHomeRoute`() {
        assertEquals(TrainerHomeRoute, topLevelDestinationsFor(UserRole.TRAINER).first().route)
    }

    @Test
    fun `trainee and trainer destination routes are disjoint`() {
        val traineeRoutes = topLevelDestinationsFor(UserRole.TRAINEE).map { it.route }.toSet()
        val trainerRoutes = topLevelDestinationsFor(UserRole.TRAINER).map { it.route }.toSet()
        assertTrue(traineeRoutes.intersect(trainerRoutes).isEmpty())
    }

    // ── label+route pairing ───────────────────────────────────────────────────

    @Test
    fun `trainee Profile destination maps to TraineeProfileRoute`() {
        val profile = topLevelDestinationsFor(UserRole.TRAINEE).single { it.label == "Profile" }
        assertEquals(TraineeProfileRoute, profile.route)
    }

    @Test
    fun `trainer Profile destination maps to TrainerProfileRoute`() {
        val profile = topLevelDestinationsFor(UserRole.TRAINER).single { it.label == "Profile" }
        assertEquals(TrainerProfileRoute, profile.route)
    }
}
