package com.example.personaltrainerapp.ui.workouts

import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.Workout
import com.example.personaltrainerapp.data.workouts.WorkoutsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// In-test fake that avoids any Firebase dependency
private class TestWorkoutsRepository(
    initialWorkouts: List<Workout> = emptyList()
) : WorkoutsRepository {

    private var workouts = initialWorkouts.toMutableList()
    var markCompleteCalled = false

    override suspend fun getAllWorkouts() = workouts.toList()

    override suspend fun getUpcomingWorkouts(traineeId: String) =
        workouts.filter { it.traineeId == traineeId }.sortedBy { it.startTime.seconds }

    override suspend fun getWorkoutById(workoutId: String) =
        workouts.find { it.id == workoutId }

    override suspend fun markExerciseComplete(workoutId: String, exerciseId: String, completed: Boolean) {
        markCompleteCalled = true
        workouts = workouts.map { w ->
            if (w.id != workoutId) w
            else w.copy(exercises = w.exercises.map { e ->
                if (e.id != exerciseId) e else e.copy(completed = completed)
            })
        }.toMutableList()
    }

    override suspend fun scheduleWorkout(workout: Workout, traineeId: String, slot: AvailabilitySlot) {}
    override suspend fun saveNewWorkout(workout: Workout) { workouts.add(workout) }
    override suspend fun getTrainersWorkouts(trainerId: String) = workouts.filter { it.trainerId == trainerId }
    override suspend fun decoupleWorkout(workoutId: String) {
        workouts = workouts.map { if (it.id == workoutId) it.copy(traineeId = "") else it }.toMutableList()
    }
}

private class ThrowingWorkoutsRepository : WorkoutsRepository {
    override suspend fun getAllWorkouts() = emptyList<Workout>()
    override suspend fun getUpcomingWorkouts(traineeId: String): List<Workout> =
        throw RuntimeException("Network error")
    override suspend fun getWorkoutById(workoutId: String) = null
    override suspend fun markExerciseComplete(workoutId: String, exerciseId: String, completed: Boolean) {}
    override suspend fun scheduleWorkout(workout: Workout, traineeId: String, slot: AvailabilitySlot) {}
    override suspend fun saveNewWorkout(workout: Workout) {}
    override suspend fun getTrainersWorkouts(trainerId: String) = emptyList<Workout>()
    override suspend fun decoupleWorkout(workoutId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: TestWorkoutsRepository
    private lateinit var viewModel: WorkoutsViewModel

    private fun makeWorkout(id: String, traineeId: String = "trainee1", trainerId: String = "trainer1") = Workout(
        id = id,
        traineeId = traineeId,
        trainerId = trainerId,
        name = "Workout $id"
    )

    private fun makeExercise(id: String, completed: Boolean = false) = Exercise(
        id = id,
        name = "Exercise $id",
        completed = completed
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = TestWorkoutsRepository()
        viewModel = WorkoutsViewModel(repository)
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
    fun `initial state has empty workouts`() {
        assertTrue(viewModel.uiState.value.workouts.isEmpty())
    }

    @Test
    fun `initial state has no selected workout`() {
        assertNull(viewModel.uiState.value.selectedWorkout)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(viewModel.uiState.value.error)
    }

    // ── loadWorkouts ─────────────────────────────────────────────────────────

    @Test
    fun `loadWorkouts returns workouts for the correct trainee`() = runTest {
        repository = TestWorkoutsRepository(listOf(
            makeWorkout("w1", traineeId = "trainee1"),
            makeWorkout("w2", traineeId = "trainee2"),
            makeWorkout("w3", traineeId = "trainee1")
        ))
        viewModel = WorkoutsViewModel(repository)

        viewModel.loadWorkouts("trainee1")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.workouts.size)
        assertTrue(state.workouts.all { it.traineeId == "trainee1" })
    }

    @Test
    fun `loadWorkouts returns empty list when trainee has no workouts`() = runTest {
        repository = TestWorkoutsRepository(listOf(makeWorkout("w1", traineeId = "other")))
        viewModel = WorkoutsViewModel(repository)

        viewModel.loadWorkouts("trainee1")

        assertTrue(viewModel.uiState.value.workouts.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadWorkouts sets error on exception`() = runTest {
        viewModel = WorkoutsViewModel(ThrowingWorkoutsRepository())

        viewModel.loadWorkouts("trainee1")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed to load workouts", state.error)
        assertTrue(state.workouts.isEmpty())
    }

    @Test
    fun `loadWorkouts clears previous error on success`() = runTest {
        val throwingRepo = ThrowingWorkoutsRepository()
        viewModel = WorkoutsViewModel(throwingRepo)
        viewModel.loadWorkouts("trainee1") // causes error

        val goodRepo = TestWorkoutsRepository()
        viewModel = WorkoutsViewModel(goodRepo)
        viewModel.loadWorkouts("trainee1") // succeeds

        assertNull(viewModel.uiState.value.error)
    }

    // ── loadAllWorkouts ──────────────────────────────────────────────────────

    @Test
    fun `loadAllWorkouts returns all workouts regardless of trainee`() = runTest {
        repository = TestWorkoutsRepository(listOf(
            makeWorkout("w1", traineeId = "trainee1"),
            makeWorkout("w2", traineeId = "trainee2")
        ))
        viewModel = WorkoutsViewModel(repository)

        viewModel.loadAllWorkouts()

        assertEquals(2, viewModel.uiState.value.workouts.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── selectWorkout / clearSelectedWorkout ─────────────────────────────────

    @Test
    fun `selectWorkout sets the selected workout`() = runTest {
        val workout = makeWorkout("w1")
        viewModel.selectWorkout(workout)
        assertEquals(workout, viewModel.uiState.value.selectedWorkout)
    }

    @Test
    fun `selectWorkout replaces previous selection`() = runTest {
        viewModel.selectWorkout(makeWorkout("w1"))
        viewModel.selectWorkout(makeWorkout("w2"))
        assertEquals("w2", viewModel.uiState.value.selectedWorkout?.id)
    }

    @Test
    fun `clearSelectedWorkout sets selectedWorkout to null`() = runTest {
        viewModel.selectWorkout(makeWorkout("w1"))
        viewModel.clearSelectedWorkout()
        assertNull(viewModel.uiState.value.selectedWorkout)
    }

    // ── toggleExerciseComplete ───────────────────────────────────────────────

    @Test
    fun `toggleExerciseComplete marks incomplete exercise as complete`() = runTest {
        val exercise = makeExercise("e1", completed = false)
        val workout = makeWorkout("w1").copy(exercises = listOf(exercise))
        repository = TestWorkoutsRepository(listOf(workout))
        viewModel = WorkoutsViewModel(repository)
        viewModel.loadWorkouts("trainee1")

        viewModel.toggleExerciseComplete("w1", "e1")

        val updatedExercise = viewModel.uiState.value.workouts.first { it.id == "w1" }
            .exercises.first { it.id == "e1" }
        assertTrue(updatedExercise.completed)
    }

    @Test
    fun `toggleExerciseComplete marks complete exercise as incomplete`() = runTest {
        val exercise = makeExercise("e1", completed = true)
        val workout = makeWorkout("w1").copy(exercises = listOf(exercise))
        repository = TestWorkoutsRepository(listOf(workout))
        viewModel = WorkoutsViewModel(repository)
        viewModel.loadWorkouts("trainee1")

        viewModel.toggleExerciseComplete("w1", "e1")

        val updatedExercise = viewModel.uiState.value.workouts.first { it.id == "w1" }
            .exercises.first { it.id == "e1" }
        assertFalse(updatedExercise.completed)
    }

    @Test
    fun `toggleExerciseComplete with invalid workoutId is a no-op`() = runTest {
        val workout = makeWorkout("w1").copy(exercises = listOf(makeExercise("e1")))
        repository = TestWorkoutsRepository(listOf(workout))
        viewModel = WorkoutsViewModel(repository)
        viewModel.loadWorkouts("trainee1")

        viewModel.toggleExerciseComplete("nonexistent", "e1")

        assertFalse(repository.markCompleteCalled)
    }

    @Test
    fun `toggleExerciseComplete with invalid exerciseId is a no-op`() = runTest {
        val workout = makeWorkout("w1").copy(exercises = listOf(makeExercise("e1")))
        repository = TestWorkoutsRepository(listOf(workout))
        viewModel = WorkoutsViewModel(repository)
        viewModel.loadWorkouts("trainee1")

        viewModel.toggleExerciseComplete("w1", "nonexistent")

        assertFalse(repository.markCompleteCalled)
    }

    @Test
    fun `toggleExerciseComplete also updates selectedWorkout when it matches`() = runTest {
        val exercise = makeExercise("e1", completed = false)
        val workout = makeWorkout("w1").copy(exercises = listOf(exercise))
        repository = TestWorkoutsRepository(listOf(workout))
        viewModel = WorkoutsViewModel(repository)
        viewModel.loadWorkouts("trainee1")
        viewModel.selectWorkout(viewModel.uiState.value.workouts.first())

        viewModel.toggleExerciseComplete("w1", "e1")

        val updatedExercise = viewModel.uiState.value.selectedWorkout
            ?.exercises?.first { it.id == "e1" }
        assertNotNull(updatedExercise)
        assertTrue(updatedExercise!!.completed)
    }

    @Test
    fun `toggleExerciseComplete does not affect exercises in other workouts`() = runTest {
        val e1 = makeExercise("e1", completed = false)
        val e2 = makeExercise("e2", completed = false)
        val w1 = makeWorkout("w1").copy(exercises = listOf(e1))
        val w2 = makeWorkout("w2").copy(exercises = listOf(e2))
        repository = TestWorkoutsRepository(listOf(w1, w2))
        viewModel = WorkoutsViewModel(repository)
        viewModel.loadWorkouts("trainee1")

        viewModel.toggleExerciseComplete("w1", "e1")

        val w2Exercise = viewModel.uiState.value.workouts.first { it.id == "w2" }.exercises.first()
        assertFalse(w2Exercise.completed)
    }

    @Test
    fun `toggleExerciseComplete does not affect other exercises within the same workout`() = runTest {
        val e1 = makeExercise("e1", completed = false)
        val e2 = makeExercise("e2", completed = false)
        val workout = makeWorkout("w1").copy(exercises = listOf(e1, e2))
        repository = TestWorkoutsRepository(listOf(workout))
        viewModel = WorkoutsViewModel(repository)
        viewModel.loadWorkouts("trainee1")

        viewModel.toggleExerciseComplete("w1", "e1")

        val e2Updated = viewModel.uiState.value.workouts.first().exercises.first { it.id == "e2" }
        assertFalse(e2Updated.completed)
    }
}