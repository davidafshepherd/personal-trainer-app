package com.example.personaltrainerapp.ui.trainees

import com.example.personaltrainerapp.data.trainees.TraineeRepository
import com.example.personaltrainerapp.data.trainer.TrainerRequest
import com.example.personaltrainerapp.data.user.UserProfile
import com.example.personaltrainerapp.data.user.UserRole
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
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
class TraineeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: TraineeRepository
    private lateinit var viewModel: TraineeViewModel

    private fun makeTrainee(id: String, trainerId: String? = null) = UserProfile(
        uid = id,
        email = "$id@trainee.com",
        displayName = "Trainee $id",
        role = UserRole.TRAINEE,
        trainerId = trainerId
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = TraineeViewModel(repository)
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
    fun `initial state has empty allTrainees`() {
        assertTrue(viewModel.uiState.value.allTrainees.isEmpty())
    }

    @Test
    fun `initial state has empty myTrainees`() {
        assertTrue(viewModel.uiState.value.myTrainees.isEmpty())
    }

    @Test
    fun `initial state has empty myRequests`() {
        assertTrue(viewModel.uiState.value.myRequests.isEmpty())
    }

    @Test
    fun `initial state has null selectedTrainee`() {
        assertNull(viewModel.uiState.value.selectedTrainee)
    }

    @Test
    fun `initial state has null tempSlot`() {
        assertNull(viewModel.uiState.value.tempSlot)
    }

    @Test
    fun `initial state has zero availability`() {
        assertEquals(0, viewModel.uiState.value.availability)
    }

    // ── loadTrainees ─────────────────────────────────────────────────────────

    @Test
    fun `loadTrainees populates allTrainees with every trainee`() = runTest {
        val trainees = listOf(makeTrainee("t1"), makeTrainee("t2"), makeTrainee("t3"))
        coEvery { repository.getAllTrainees() } returns trainees

        viewModel.loadTrainees("trainer1")

        assertEquals(3, viewModel.uiState.value.allTrainees.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadTrainees filters myTrainees to only this trainer's trainees`() = runTest {
        val trainees = listOf(
            makeTrainee("t1", trainerId = "trainer1"),
            makeTrainee("t2", trainerId = "trainer2"),
            makeTrainee("t3", trainerId = "trainer1"),
            makeTrainee("t4", trainerId = null)
        )
        coEvery { repository.getAllTrainees() } returns trainees

        viewModel.loadTrainees("trainer1")

        val myTrainees = viewModel.uiState.value.myTrainees
        assertEquals(2, myTrainees.size)
        assertTrue(myTrainees.all { it.trainerId == "trainer1" })
    }

    @Test
    fun `loadTrainees with no matching trainer returns empty myTrainees`() = runTest {
        val trainees = listOf(makeTrainee("t1", trainerId = "other"))
        coEvery { repository.getAllTrainees() } returns trainees

        viewModel.loadTrainees("trainer1")

        assertTrue(viewModel.uiState.value.myTrainees.isEmpty())
        assertEquals(1, viewModel.uiState.value.allTrainees.size)
    }

    @Test
    fun `loadTrainees with empty repository returns empty lists`() = runTest {
        coEvery { repository.getAllTrainees() } returns emptyList()

        viewModel.loadTrainees("trainer1")

        assertTrue(viewModel.uiState.value.allTrainees.isEmpty())
        assertTrue(viewModel.uiState.value.myTrainees.isEmpty())
    }

    // ── setTempSlot ──────────────────────────────────────────────────────────

    @Test
    fun `setTempSlot creates slot with correct traineeId`() = runTest {
        val date = LocalDate.of(2026, 5, 15)

        viewModel.setTempSlot("trainee1", date, 14)

        assertEquals("trainee1", viewModel.uiState.value.tempSlot?.traineeId)
    }

    @Test
    fun `setTempSlot creates slot with correct date`() = runTest {
        val date = LocalDate.of(2026, 5, 15)

        viewModel.setTempSlot("trainee1", date, 14)

        assertEquals(date, viewModel.uiState.value.tempSlot?.date)
    }

    @Test
    fun `setTempSlot creates slot with correct hour`() = runTest {
        val date = LocalDate.of(2026, 5, 15)

        viewModel.setTempSlot("trainee1", date, 14)

        assertEquals(14, viewModel.uiState.value.tempSlot?.hour)
    }

    @Test
    fun `setTempSlot replaces previous tempSlot`() = runTest {
        val date = LocalDate.of(2026, 5, 15)

        viewModel.setTempSlot("trainee1", date, 9)
        viewModel.setTempSlot("trainee2", date, 14)

        val slot = viewModel.uiState.value.tempSlot
        assertEquals("trainee2", slot?.traineeId)
        assertEquals(14, slot?.hour)
    }

    // ── assignTrainee ────────────────────────────────────────────────────────

    @Test
    fun `assignTrainee calls repository with traineeId first then trainerId`() = runTest {
        coEvery { repository.updateTrainer(any(), any()) } just Runs

        viewModel.assignTrainee("trainer1", "trainee1")

        coVerify { repository.updateTrainer("trainee1", "trainer1") }
    }

    // ── loadRequests ─────────────────────────────────────────────────────────

    @Test
    fun `loadRequests populates myRequests`() = runTest {
        val requests = listOf(
            TrainerRequest("r1", "trainee1", "trainer1", "PENDING"),
            TrainerRequest("r2", "trainee2", "trainer1", "PENDING")
        )
        coEvery { repository.getRequests("trainer1") } returns requests

        viewModel.loadRequests("trainer1")

        assertEquals(2, viewModel.uiState.value.myRequests.size)
    }

    @Test
    fun `loadRequests with no requests returns empty list`() = runTest {
        coEvery { repository.getRequests("trainer1") } returns emptyList()

        viewModel.loadRequests("trainer1")

        assertTrue(viewModel.uiState.value.myRequests.isEmpty())
    }

    @Test
    fun `loadRequests preserves request data`() = runTest {
        val request = TrainerRequest("r1", "trainee1", "trainer1", "PENDING")
        coEvery { repository.getRequests("trainer1") } returns listOf(request)

        viewModel.loadRequests("trainer1")

        assertEquals(request, viewModel.uiState.value.myRequests.first())
    }

    // ── selectTrainee ────────────────────────────────────────────────────────

    @Test
    fun `selectTrainee sets selectedTrainee`() = runTest {
        val trainee = makeTrainee("t1")
        coEvery { repository.getTraineeById("t1") } returns trainee

        viewModel.selectTrainee("t1")

        assertEquals(trainee, viewModel.uiState.value.selectedTrainee)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `selectTrainee with nonexistent id sets selectedTrainee to null`() = runTest {
        coEvery { repository.getTraineeById("nonexistent") } returns null

        viewModel.selectTrainee("nonexistent")

        assertNull(viewModel.uiState.value.selectedTrainee)
    }

    // ── loadAvailability ─────────────────────────────────────────────────────

    @Test
    fun `loadAvailability sets availability from repository`() = runTest {
        coEvery { repository.getTrainerAvailability("trainer1") } returns 5

        viewModel.loadAvailability("trainer1")

        assertEquals(5, viewModel.uiState.value.availability)
    }

    @Test
    fun `loadAvailability sets zero when repository returns zero`() = runTest {
        coEvery { repository.getTrainerAvailability("trainer1") } returns 0

        viewModel.loadAvailability("trainer1")

        assertEquals(0, viewModel.uiState.value.availability)
    }

    // ── updateRequestStatus ──────────────────────────────────────────────────

    @Test
    fun `updateRequestStatus calls repository with correct parameters`() = runTest {
        every { repository.updateRequestStatus(any(), any()) } just Runs

        viewModel.updateRequestStatus("req1", "ACCEPTED")

        verify { repository.updateRequestStatus("req1", "ACCEPTED") }
    }

    @Test
    fun `updateRequestStatus with REJECTED status calls repository`() = runTest {
        every { repository.updateRequestStatus(any(), any()) } just Runs

        viewModel.updateRequestStatus("req2", "REJECTED")

        verify { repository.updateRequestStatus("req2", "REJECTED") }
    }

    // ── setTrainerAvailability ───────────────────────────────────────────────

    @Test
    fun `setTrainerAvailability calls repository with correct parameters`() = runTest {
        coEvery { repository.setTrainerAvailability(any(), any()) } returns true

        viewModel.setTrainerAvailability("trainer1", 10)

        coVerify { repository.setTrainerAvailability("trainer1", 10) }
    }
}
