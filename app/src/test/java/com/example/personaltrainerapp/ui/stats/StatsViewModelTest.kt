package com.example.personaltrainerapp.ui.stats

import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.trainees.TraineeRepository
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.data.workouts.WorkoutsRepository
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

private class TestWorkoutsRepository(
    private val workouts: List<Workout> = emptyList()
) : WorkoutsRepository {
    override suspend fun getTrainersWorkouts(trainerId: String) = workouts
    override suspend fun getAllWorkouts() = workouts
    override suspend fun getUpcomingWorkouts(traineeId: String) = emptyList<Workout>()
    override suspend fun getWorkoutById(workoutId: String) = null
    override suspend fun markExerciseComplete(workoutId: String, exerciseId: String, completed: Boolean) {}
    override suspend fun scheduleWorkout(workout: Workout, traineeId: String, slot: AvailabilitySlot) {}
    override suspend fun saveNewWorkout(workout: Workout) {}
    override suspend fun decoupleWorkout(workoutId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var workoutsRepo: TestWorkoutsRepository
    private lateinit var traineeRepo: TraineeRepository
    private lateinit var viewModel: StatsViewModel

    private fun pastDays(days: Int): Timestamp =
        Timestamp(Date(System.currentTimeMillis() - days * 86_400_000L))

    private fun futureDays(days: Int): Timestamp =
        Timestamp(Date(System.currentTimeMillis() + days * 86_400_000L))

    private fun makeTrainee(id: String, trainerId: String = "trainer1") = UserProfile(
        uid = id,
        email = "$id@trainee.com",
        displayName = "Trainee $id",
        role = UserRole.TRAINEE,
        trainerId = trainerId
    )

    // Workout in the past with all exercises completed
    private fun completedWorkout(id: String, traineeId: String, trainerId: String = "trainer1") = Workout(
        id = id, traineeId = traineeId, trainerId = trainerId,
        name = "Workout $id",
        startTime = pastDays(3),
        endTime = pastDays(3),
        exercises = listOf(
            Exercise(id = "${id}_e1", completed = true),
            Exercise(id = "${id}_e2", completed = true)
        )
    )

    // Workout in the past with only some exercises completed
    private fun incompleteWorkout(id: String, traineeId: String, trainerId: String = "trainer1") = Workout(
        id = id, traineeId = traineeId, trainerId = trainerId,
        name = "Workout $id",
        startTime = pastDays(3),
        endTime = pastDays(3),
        exercises = listOf(
            Exercise(id = "${id}_e1", completed = false),
            Exercise(id = "${id}_e2", completed = true)
        )
    )

    // Workout whose startTime is in the future
    private fun futureWorkout(id: String, traineeId: String, trainerId: String = "trainer1") = Workout(
        id = id, traineeId = traineeId, trainerId = trainerId,
        name = "Workout $id",
        startTime = futureDays(7),
        endTime = futureDays(7),
        exercises = listOf(Exercise(id = "${id}_e1", completed = false))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workoutsRepo = TestWorkoutsRepository()
        traineeRepo = mockk()
        viewModel = StatsViewModel(workoutsRepo, traineeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading true`() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial state has empty allWeeklyCompletions`() {
        assertTrue(viewModel.uiState.value.allWeeklyCompletions.isEmpty())
    }

    @Test
    fun `initial state has empty perTraineeWeekly`() {
        assertTrue(viewModel.uiState.value.perTraineeWeekly.isEmpty())
    }

    @Test
    fun `initial state has empty traineeStats`() {
        assertTrue(viewModel.uiState.value.traineeStats.isEmpty())
    }

    @Test
    fun `initial state has empty traineeOptions`() {
        assertTrue(viewModel.uiState.value.traineeOptions.isEmpty())
    }

    @Test
    fun `initial state has zero totalWorkouts`() {
        assertEquals(0, viewModel.uiState.value.totalWorkouts)
    }

    @Test
    fun `initial state has zero completedWorkouts`() {
        assertEquals(0, viewModel.uiState.value.completedWorkouts)
    }

    @Test
    fun `initial state has zero weekOffset`() {
        assertEquals(0, viewModel.uiState.value.weekOffset)
    }

    // ── WeeklyCompletion model ───────────────────────────────────────────────

    @Test
    fun `WeeklyCompletion stores label completed and total`() {
        val wc = WeeklyCompletion(label = "4 May 26", completed = 3, total = 5)
        assertEquals("4 May 26", wc.label)
        assertEquals(3, wc.completed)
        assertEquals(5, wc.total)
    }

    @Test
    fun `WeeklyCompletion equality holds for identical data`() {
        val wc1 = WeeklyCompletion("4 May 26", completed = 3, total = 5)
        val wc2 = WeeklyCompletion("4 May 26", completed = 3, total = 5)
        assertEquals(wc1, wc2)
    }

    @Test
    fun `WeeklyCompletion copy produces independent modified copy`() {
        val original = WeeklyCompletion("4 May 26", completed = 1, total = 2)
        val copy = original.copy(completed = 2)
        assertEquals(1, original.completed)
        assertEquals(2, copy.completed)
    }

    // ── TraineeStats model ───────────────────────────────────────────────────

    @Test
    fun `TraineeStats completionRate is zero when totalWorkouts is zero`() {
        val stats = TraineeStats("Alice", completedWorkouts = 0, totalWorkouts = 0)
        assertEquals(0f, stats.completionRate, 0.001f)
    }

    @Test
    fun `TraineeStats completionRate is 1 when all workouts are completed`() {
        val stats = TraineeStats("Alice", completedWorkouts = 4, totalWorkouts = 4)
        assertEquals(1f, stats.completionRate, 0.001f)
    }

    @Test
    fun `TraineeStats completionRate is 0 point 5 when half completed`() {
        val stats = TraineeStats("Alice", completedWorkouts = 2, totalWorkouts = 4)
        assertEquals(0.5f, stats.completionRate, 0.001f)
    }

    @Test
    fun `TraineeStats completionRate is proportional to completed over total`() {
        val stats = TraineeStats("Alice", completedWorkouts = 1, totalWorkouts = 3)
        assertEquals(1f / 3f, stats.completionRate, 0.001f)
    }

    // ── StatsUiState.overallCompletionRate ───────────────────────────────────

    @Test
    fun `overallCompletionRate is zero when no workouts`() {
        val state = StatsUiState(totalWorkouts = 0, completedWorkouts = 0)
        assertEquals(0f, state.overallCompletionRate, 0.001f)
    }

    @Test
    fun `overallCompletionRate is 1 when all workouts completed`() {
        val state = StatsUiState(totalWorkouts = 5, completedWorkouts = 5)
        assertEquals(1f, state.overallCompletionRate, 0.001f)
    }

    @Test
    fun `overallCompletionRate is 0 point 5 when half completed`() {
        val state = StatsUiState(totalWorkouts = 4, completedWorkouts = 2)
        assertEquals(0.5f, state.overallCompletionRate, 0.001f)
    }

    // ── load ─────────────────────────────────────────────────────────────────

    @Test
    fun `load sets isLoading to false`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `load excludes future workouts from totalWorkouts`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(futureWorkout("w1", "t1"))), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(0, viewModel.uiState.value.totalWorkouts)
    }

    @Test
    fun `load excludes workouts with no traineeId from totalWorkouts`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        val unassigned = Workout(id = "w1", traineeId = "", trainerId = "trainer1", startTime = pastDays(3))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(unassigned)), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(0, viewModel.uiState.value.totalWorkouts)
    }

    @Test
    fun `load counts past assigned workouts in totalWorkouts`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"), makeTrainee("t2"))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(
            completedWorkout("w1", "t1"),
            incompleteWorkout("w2", "t1"),
            completedWorkout("w3", "t2")
        )), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(3, viewModel.uiState.value.totalWorkouts)
    }

    @Test
    fun `load counts fully completed workouts in completedWorkouts`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(
            completedWorkout("w1", "t1"),
            completedWorkout("w2", "t1"),
            incompleteWorkout("w3", "t1")
        )), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(2, viewModel.uiState.value.completedWorkouts)
    }

    @Test
    fun `load does not count workouts with no exercises as completed`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        val emptyExercisesWorkout = Workout(
            id = "w1", traineeId = "t1", trainerId = "trainer1",
            startTime = pastDays(3), exercises = emptyList()
        )
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(emptyExercisesWorkout)), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(0, viewModel.uiState.value.completedWorkouts)
    }

    @Test
    fun `load with all completed workouts has completedWorkouts equal to totalWorkouts`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(
            completedWorkout("w1", "t1"),
            completedWorkout("w2", "t1"),
            completedWorkout("w3", "t1")
        )), traineeRepo)

        viewModel.load("trainer1")

        val state = viewModel.uiState.value
        assertEquals(state.totalWorkouts, state.completedWorkouts)
    }

    @Test
    fun `load populates traineeOptions with trainer's trainees`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(
            makeTrainee("t1"), makeTrainee("t2"), makeTrainee("t3")
        )
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(3, viewModel.uiState.value.traineeOptions.size)
    }

    @Test
    fun `load populates traineeOptions with correct uid and displayName`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        val option = viewModel.uiState.value.traineeOptions.first()
        assertEquals("t1", option.first)
        assertEquals("Trainee t1", option.second)
    }

    @Test
    fun `load allWeeklyCompletions has exactly 8 entries`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(8, viewModel.uiState.value.allWeeklyCompletions.size)
    }

    @Test
    fun `load recent completed workout appears in weekly completion buckets`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(completedWorkout("w1", "t1"))), traineeRepo)

        viewModel.load("trainer1")

        val totalInBuckets = viewModel.uiState.value.allWeeklyCompletions.sumOf { it.completed }
        assertEquals(1, totalInBuckets)
    }

    @Test
    fun `load populates perTraineeWeekly keyed by trainee uid`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"), makeTrainee("t2"))
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        val keys = viewModel.uiState.value.perTraineeWeekly.keys
        assertTrue(keys.contains("t1"))
        assertTrue(keys.contains("t2"))
    }

    @Test
    fun `load each perTraineeWeekly entry has exactly 8 buckets`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(8, viewModel.uiState.value.perTraineeWeekly["t1"]?.size)
    }

    @Test
    fun `load per-trainee weekly buckets reflect that trainee's completed workouts`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(
            completedWorkout("w1", "t1"),
            completedWorkout("w2", "t1")
        )), traineeRepo)

        viewModel.load("trainer1")

        val totalInBuckets = viewModel.uiState.value.perTraineeWeekly["t1"]
            ?.sumOf { it.completed } ?: 0
        assertEquals(2, totalInBuckets)
    }

    @Test
    fun `load sets non-empty windowLabel`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        assertTrue(viewModel.uiState.value.windowLabel.isNotBlank())
    }

    @Test
    fun `load sorts traineeStats by completionRate descending`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(
            makeTrainee("t1"), makeTrainee("t2"), makeTrainee("t3")
        )
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(
            completedWorkout("w1", "t1"),               // t1: 1/1 = 100%
            completedWorkout("w2", "t2"),
            incompleteWorkout("w3", "t2"),               // t2: 1/2 = 50%
            incompleteWorkout("w4", "t3")                // t3: 0/1 = 0%
        )), traineeRepo)

        viewModel.load("trainer1")

        val rates = viewModel.uiState.value.traineeStats.map { it.completionRate }
        assertTrue(rates[0] >= rates[1])
        assertTrue(rates[1] >= rates[2])
    }

    @Test
    fun `load with no workouts results in zero totals`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)

        viewModel.load("trainer1")

        assertEquals(0, viewModel.uiState.value.totalWorkouts)
        assertEquals(0, viewModel.uiState.value.completedWorkouts)
    }

    @Test
    fun `load with no trainees results in empty traineeStats`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(completedWorkout("w1", "t1"))), traineeRepo)

        viewModel.load("trainer1")

        assertTrue(viewModel.uiState.value.traineeStats.isEmpty())
    }

    // ── shiftWeeks ───────────────────────────────────────────────────────────

    @Test
    fun `shiftWeeks with positive delta increases weekOffset`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)
        viewModel.load("trainer1")

        viewModel.shiftWeeks(8)

        assertEquals(8, viewModel.uiState.value.weekOffset)
    }

    @Test
    fun `shiftWeeks does not decrease weekOffset below zero`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)
        viewModel.load("trainer1") // starts at weekOffset 0

        viewModel.shiftWeeks(-8)

        assertEquals(0, viewModel.uiState.value.weekOffset)
    }

    @Test
    fun `shiftWeeks forward then back returns to original offset`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)
        viewModel.load("trainer1")

        viewModel.shiftWeeks(8)
        viewModel.shiftWeeks(-8)

        assertEquals(0, viewModel.uiState.value.weekOffset)
    }

    @Test
    fun `shiftWeeks accumulates across multiple calls`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)
        viewModel.load("trainer1")

        viewModel.shiftWeeks(8)
        viewModel.shiftWeeks(8)

        assertEquals(16, viewModel.uiState.value.weekOffset)
    }

    @Test
    fun `shiftWeeks still produces 8 weekly buckets`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)
        viewModel.load("trainer1")

        viewModel.shiftWeeks(8)

        assertEquals(8, viewModel.uiState.value.allWeeklyCompletions.size)
    }

    @Test
    fun `shiftWeeks updates windowLabel`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns emptyList()
        viewModel = StatsViewModel(TestWorkoutsRepository(), traineeRepo)
        viewModel.load("trainer1")
        val originalLabel = viewModel.uiState.value.windowLabel

        viewModel.shiftWeeks(8)

        assertNotEquals(originalLabel, viewModel.uiState.value.windowLabel)
    }

    @Test
    fun `shiftWeeks back moves recent workouts outside the displayed window`() = runTest {
        coEvery { traineeRepo.getAllTrainees() } returns listOf(makeTrainee("t1"))
        viewModel = StatsViewModel(TestWorkoutsRepository(listOf(completedWorkout("w1", "t1"))), traineeRepo)
        viewModel.load("trainer1")
        val beforeShift = viewModel.uiState.value.allWeeklyCompletions.sumOf { it.completed }
        assertEquals(1, beforeShift) // workout 3 days ago is in the current window

        viewModel.shiftWeeks(8) // move window 8 weeks back

        val afterShift = viewModel.uiState.value.allWeeklyCompletions.sumOf { it.completed }
        assertEquals(0, afterShift) // workout 3 days ago is no longer in this window
    }
}
