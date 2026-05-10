package com.example.personaltrainerapp.ui.library

import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.exercises.ExerciseRepository
import com.example.personaltrainerapp.data.workouts.Exercise
import com.example.personaltrainerapp.data.workouts.RealWorkoutsRepository
import com.example.personaltrainerapp.data.workouts.Workout
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var wRepository: RealWorkoutsRepository
    private lateinit var eRepository: ExerciseRepository
    private lateinit var viewModel: LibraryViewModel

    private fun makeWorkout(id: String, traineeId: String = "") = Workout(
        id = id,
        trainerId = "trainer1",
        traineeId = traineeId,
        name = "Workout $id"
    )

    private fun makeExercise(id: String) = Exercise(
        id = id,
        name = "Exercise $id",
        trainerId = "trainer1"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        wRepository = mockk()
        eRepository = mockk()
        viewModel = LibraryViewModel(wRepository, eRepository)
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
    fun `initial state has empty exercises`() {
        assertTrue(viewModel.uiState.value.exercises.isEmpty())
    }

    @Test
    fun `initial state has null selectedWorkout`() {
        assertNull(viewModel.uiState.value.selectedWorkout)
    }

    @Test
    fun `initial state has null selectedExercise`() {
        assertNull(viewModel.uiState.value.selectedExercise)
    }

    // ── loadAll ──────────────────────────────────────────────────────────────

    @Test
    fun `loadAll populates workouts`() = runTest {
        val workouts = listOf(makeWorkout("w1"), makeWorkout("w2"))
        coEvery { wRepository.getTrainersWorkouts("trainer1") } returns workouts
        coEvery { eRepository.getAllExercises("trainer1") } returns emptyList()

        viewModel.loadAll("trainer1")

        assertEquals(2, viewModel.uiState.value.workouts.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadAll populates exercises`() = runTest {
        val exercises = listOf(makeExercise("e1"), makeExercise("e2"), makeExercise("e3"))
        coEvery { wRepository.getTrainersWorkouts("trainer1") } returns emptyList()
        coEvery { eRepository.getAllExercises("trainer1") } returns exercises

        viewModel.loadAll("trainer1")

        assertEquals(3, viewModel.uiState.value.exercises.size)
    }

    @Test
    fun `loadAll with empty repositories sets empty lists`() = runTest {
        coEvery { wRepository.getTrainersWorkouts(any()) } returns emptyList()
        coEvery { eRepository.getAllExercises(any()) } returns emptyList()

        viewModel.loadAll("trainer1")

        assertTrue(viewModel.uiState.value.workouts.isEmpty())
        assertTrue(viewModel.uiState.value.exercises.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── selectWorkout ────────────────────────────────────────────────────────

    @Test
    fun `selectWorkout sets selectedWorkout`() = runTest {
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
    fun `selectWorkout does not change selectedExercise`() = runTest {
        val exercise = makeExercise("e1")
        viewModel.selectExercise(exercise)
        viewModel.selectWorkout(makeWorkout("w1"))

        assertEquals(exercise, viewModel.uiState.value.selectedExercise)
    }

    // ── selectExercise ───────────────────────────────────────────────────────

    @Test
    fun `selectExercise sets selectedExercise`() = runTest {
        val exercise = makeExercise("e1")

        viewModel.selectExercise(exercise)

        assertEquals(exercise, viewModel.uiState.value.selectedExercise)
    }

    @Test
    fun `selectExercise replaces previous selection`() = runTest {
        viewModel.selectExercise(makeExercise("e1"))
        viewModel.selectExercise(makeExercise("e2"))

        assertEquals("e2", viewModel.uiState.value.selectedExercise?.id)
    }

    // ── saveExercise ─────────────────────────────────────────────────────────

    @Test
    fun `saveExercise calls repository`() = runTest {
        val exercise = makeExercise("e1")
        coEvery { eRepository.saveNewExercise(exercise) } just Runs

        viewModel.saveExercise(exercise)

        coVerify { eRepository.saveNewExercise(exercise) }
    }

    // ── saveWorkout ──────────────────────────────────────────────────────────

    @Test
    fun `saveWorkout calls repository`() = runTest {
        val workout = makeWorkout("w1")
        coEvery { wRepository.saveNewWorkout(workout) } just Runs

        viewModel.saveWorkout(workout)

        coVerify { wRepository.saveNewWorkout(workout) }
    }

    // ── removeWorkout ────────────────────────────────────────────────────────

    @Test
    fun `removeWorkout sets traineeId to empty string for the target workout`() = runTest {
        val workouts = listOf(
            makeWorkout("w1", traineeId = "trainee1"),
            makeWorkout("w2", traineeId = "trainee2")
        )
        coEvery { wRepository.getTrainersWorkouts("trainer1") } returns workouts
        coEvery { eRepository.getAllExercises("trainer1") } returns emptyList()
        viewModel.loadAll("trainer1")
        coEvery { wRepository.decoupleWorkout("w1") } just Runs

        viewModel.removeWorkout("w1")

        val w1 = viewModel.uiState.value.workouts.first { it.id == "w1" }
        assertEquals("", w1.traineeId)
    }

    @Test
    fun `removeWorkout does not affect other workouts`() = runTest {
        val workouts = listOf(
            makeWorkout("w1", traineeId = "trainee1"),
            makeWorkout("w2", traineeId = "trainee2")
        )
        coEvery { wRepository.getTrainersWorkouts("trainer1") } returns workouts
        coEvery { eRepository.getAllExercises("trainer1") } returns emptyList()
        viewModel.loadAll("trainer1")
        coEvery { wRepository.decoupleWorkout("w1") } just Runs

        viewModel.removeWorkout("w1")

        val w2 = viewModel.uiState.value.workouts.first { it.id == "w2" }
        assertEquals("trainee2", w2.traineeId)
    }

    @Test
    fun `removeWorkout calls decoupleWorkout on repository`() = runTest {
        val workouts = listOf(makeWorkout("w1", traineeId = "trainee1"))
        coEvery { wRepository.getTrainersWorkouts(any()) } returns workouts
        coEvery { eRepository.getAllExercises(any()) } returns emptyList()
        viewModel.loadAll("trainer1")
        coEvery { wRepository.decoupleWorkout("w1") } just Runs

        viewModel.removeWorkout("w1")

        coVerify { wRepository.decoupleWorkout("w1") }
    }

    // ── scheduleWorkout ──────────────────────────────────────────────────────

    @Test
    fun `scheduleWorkout calls repository scheduleWorkout`() = runTest {
        val workout = makeWorkout("w1")
        val slot = AvailabilitySlot("s1", "trainee1", LocalDate.of(2026, 5, 10), 9)
        coEvery { wRepository.scheduleWorkout(any(), any(), any()) } just Runs
        coEvery { wRepository.getTrainersWorkouts("trainer1") } returns emptyList()
        coEvery { eRepository.getAllExercises("trainer1") } returns emptyList()

        viewModel.scheduleWorkout(workout, "trainee1", "trainer1", slot)

        coVerify { wRepository.scheduleWorkout(workout, "trainee1", slot) }
    }
}
