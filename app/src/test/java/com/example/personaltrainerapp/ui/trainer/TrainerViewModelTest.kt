package com.example.personaltrainerapp.ui.trainer

import com.example.personaltrainerapp.data.trainer.TrainerRepository
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
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

@OptIn(ExperimentalCoroutinesApi::class)
class TrainerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: TrainerRepository
    private lateinit var viewModel: TrainerViewModel

    private fun makeTrainer(id: String) = UserProfile(
        uid = id,
        email = "$id@trainer.com",
        displayName = "Trainer $id",
        role = UserRole.TRAINER
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = TrainerViewModel(repository)
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
    fun `initial state has empty trainers list`() {
        assertTrue(viewModel.uiState.value.trainers.isEmpty())
    }

    @Test
    fun `initial state has null selectedTrainer`() {
        assertNull(viewModel.uiState.value.selectedTrainer)
    }

    @Test
    fun `initial state has empty pendingRequestedTrainerIds`() {
        assertTrue(viewModel.uiState.value.pendingRequestedTrainerIds.isEmpty())
    }

    // ── loadTrainers ─────────────────────────────────────────────────────────

    @Test
    fun `loadTrainers populates trainers list`() = runTest {
        coEvery { repository.getAllTrainers() } returns listOf(makeTrainer("t1"), makeTrainer("t2"))

        viewModel.loadTrainers()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.trainers.size)
    }

    @Test
    fun `loadTrainers preserves trainer data`() = runTest {
        val trainer = makeTrainer("t1")
        coEvery { repository.getAllTrainers() } returns listOf(trainer)

        viewModel.loadTrainers()

        assertEquals(trainer, viewModel.uiState.value.trainers.first())
    }

    @Test
    fun `loadTrainers with empty result leaves empty list`() = runTest {
        coEvery { repository.getAllTrainers() } returns emptyList()

        viewModel.loadTrainers()

        assertTrue(viewModel.uiState.value.trainers.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ── requestTrainer ───────────────────────────────────────────────────────

    @Test
    fun `requestTrainer adds trainer uid to pendingRequestedTrainerIds`() = runTest {
        coEvery { repository.requestTrainer(any(), any()) } just Runs

        viewModel.requestTrainer("trainee1", makeTrainer("t1"))

        assertTrue(viewModel.uiState.value.pendingRequestedTrainerIds.contains("t1"))
    }

    @Test
    fun `requestTrainer accumulates multiple trainer ids`() = runTest {
        coEvery { repository.requestTrainer(any(), any()) } just Runs

        viewModel.requestTrainer("trainee1", makeTrainer("t1"))
        viewModel.requestTrainer("trainee1", makeTrainer("t2"))

        val pending = viewModel.uiState.value.pendingRequestedTrainerIds
        assertTrue(pending.contains("t1"))
        assertTrue(pending.contains("t2"))
        assertEquals(2, pending.size)
    }

    @Test
    fun `requestTrainer calls repository with correct trainee and trainer ids`() = runTest {
        coEvery { repository.requestTrainer(any(), any()) } just Runs

        viewModel.requestTrainer("trainee1", makeTrainer("t1"))

        coVerify { repository.requestTrainer("trainee1", "t1") }
    }

    @Test
    fun `requesting same trainer twice does not duplicate in pending set`() = runTest {
        coEvery { repository.requestTrainer(any(), any()) } just Runs

        viewModel.requestTrainer("trainee1", makeTrainer("t1"))
        viewModel.requestTrainer("trainee1", makeTrainer("t1"))

        assertEquals(1, viewModel.uiState.value.pendingRequestedTrainerIds.size)
    }

    // ── loadAssignedTrainer ──────────────────────────────────────────────────

    @Test
    fun `loadAssignedTrainer sets selectedTrainer`() = runTest {
        val trainer = makeTrainer("t1")
        coEvery { repository.getAssignedTrainer("trainee1") } returns trainer

        viewModel.loadAssignedTrainer("trainee1")

        assertEquals(trainer, viewModel.uiState.value.selectedTrainer)
    }

    @Test
    fun `loadAssignedTrainer with null result leaves selectedTrainer null`() = runTest {
        coEvery { repository.getAssignedTrainer("trainee1") } returns null

        viewModel.loadAssignedTrainer("trainee1")

        assertNull(viewModel.uiState.value.selectedTrainer)
    }

    // ── unenrolFromTrainer ───────────────────────────────────────────────────

    @Test
    fun `unenrolFromTrainer sets selectedTrainer to null`() = runTest {
        coEvery { repository.getAssignedTrainer("trainee1") } returns makeTrainer("t1")
        coEvery { repository.unenrolFromTrainer(any()) } just Runs
        viewModel.loadAssignedTrainer("trainee1")

        viewModel.unenrolFromTrainer("trainee1")

        assertNull(viewModel.uiState.value.selectedTrainer)
    }

    @Test
    fun `unenrolFromTrainer calls repository with correct traineeId`() = runTest {
        coEvery { repository.unenrolFromTrainer(any()) } just Runs

        viewModel.unenrolFromTrainer("trainee1")

        coVerify { repository.unenrolFromTrainer("trainee1") }
    }

    // ── checkPendingRequest ──────────────────────────────────────────────────

    @Test
    fun `checkPendingRequest adds to pending set when request exists`() = runTest {
        coEvery { repository.hasPendingTrainerRequest("trainee1", "t1") } returns true

        viewModel.checkPendingRequest("trainee1", "t1")

        assertTrue(viewModel.uiState.value.pendingRequestedTrainerIds.contains("t1"))
    }

    @Test
    fun `checkPendingRequest does not add when no pending request`() = runTest {
        coEvery { repository.hasPendingTrainerRequest("trainee1", "t1") } returns false

        viewModel.checkPendingRequest("trainee1", "t1")

        assertFalse(viewModel.uiState.value.pendingRequestedTrainerIds.contains("t1"))
    }

    @Test
    fun `checkPendingRequest preserves existing pending trainer ids`() = runTest {
        coEvery { repository.requestTrainer(any(), any()) } just Runs
        coEvery { repository.hasPendingTrainerRequest("trainee1", "t2") } returns true
        viewModel.requestTrainer("trainee1", makeTrainer("t1"))

        viewModel.checkPendingRequest("trainee1", "t2")

        val pending = viewModel.uiState.value.pendingRequestedTrainerIds
        assertTrue(pending.contains("t1"))
        assertTrue(pending.contains("t2"))
    }
}
