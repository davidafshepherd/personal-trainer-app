package com.example.personaltrainerapp.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {

    private val traineeRoutes = listOf(
        TraineeHomeRoute, TraineeCalendarRoute, TraineeWorkoutsRoute,
        TraineeTrainerRoute, TraineeProfileRoute
    )
    private val trainerRoutes = listOf(
        TrainerHomeRoute, TrainerTraineesRoute, TrainerLibraryRoute,
        TrainerStatsRoute, TrainerProfileRoute
    )

    @Test
    fun `there are exactly 5 trainee routes`() {
        assertEquals(5, traineeRoutes.size)
    }

    @Test
    fun `there are exactly 5 trainer routes`() {
        assertEquals(5, trainerRoutes.size)
    }

    @Test
    fun `all trainee routes are distinct`() {
        assertEquals(traineeRoutes.size, traineeRoutes.toSet().size)
    }

    @Test
    fun `all trainer routes are distinct`() {
        assertEquals(trainerRoutes.size, trainerRoutes.toSet().size)
    }

    @Test
    fun `trainee and trainer routes do not overlap`() {
        val intersection = traineeRoutes.intersect(trainerRoutes.toSet())
        assertTrue(intersection.isEmpty())
    }

    @Test
    fun `all 10 routes combined are distinct`() {
        val all = traineeRoutes + trainerRoutes
        assertEquals(10, all.toSet().size)
    }

    @Test
    fun `TraineeHomeRoute identity is stable`() {
        val a: Any = TraineeHomeRoute
        val b: Any = TraineeHomeRoute
        assertTrue(a === b)
    }

    @Test
    fun `TrainerHomeRoute identity is stable`() {
        val a: Any = TrainerHomeRoute
        val b: Any = TrainerHomeRoute
        assertTrue(a === b)
    }

    @Test
    fun `TraineeHomeRoute and TrainerHomeRoute are different objects`() {
        assertTrue((TraineeHomeRoute as Any) !== (TrainerHomeRoute as Any))
    }
}
