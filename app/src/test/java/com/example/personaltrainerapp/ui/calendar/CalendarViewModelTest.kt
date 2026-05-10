package com.example.personaltrainerapp.ui.calendar

import com.example.personaltrainerapp.data.calendar.AvailabilityRepository
import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.example.personaltrainerapp.data.calendar.AvailabilityStatus
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
class CalendarViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AvailabilityRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = CalendarViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── availabilityKey ──────────────────────────────────────────────────────

    @Test
    fun `availabilityKey produces date underscore hour format`() {
        val date = LocalDate.of(2026, 5, 10)
        assertEquals("2026-05-10_9", availabilityKey(date, 9))
    }

    @Test
    fun `availabilityKey works for hour zero`() {
        val date = LocalDate.of(2026, 1, 1)
        assertEquals("2026-01-01_0", availabilityKey(date, 0))
    }

    @Test
    fun `availabilityKey works for hour 23`() {
        val date = LocalDate.of(2026, 12, 31)
        assertEquals("2026-12-31_23", availabilityKey(date, 23))
    }

    @Test
    fun `availabilityKey is unique per slot`() {
        val date = LocalDate.of(2026, 5, 10)
        assertFalse(availabilityKey(date, 9) == availabilityKey(date, 10))
        assertFalse(availabilityKey(date, 9) == availabilityKey(date.plusDays(1), 9))
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading true`() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial state has empty slots`() {
        assertTrue(viewModel.uiState.value.slots.isEmpty())
    }

    @Test
    fun `initial state has no error`() {
        assertNull(viewModel.uiState.value.error)
    }

    // ── loadAvailability ─────────────────────────────────────────────────────

    @Test
    fun `loadAvailability populates slots on success`() = runTest {
        val date = LocalDate.of(2026, 5, 10)
        val slots = listOf(
            AvailabilitySlot("id1", "trainee1", date, 9, AvailabilityStatus.IDEAL),
            AvailabilitySlot("id2", "trainee1", date, 10, AvailabilityStatus.BUSY)
        )
        coEvery { repository.getAvailabilitySlots("trainee1", date, date) } returns slots

        viewModel.loadAvailability("trainee1", date, date)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.slots.size)
        assertEquals(AvailabilityStatus.IDEAL, state.slots[availabilityKey(date, 9)])
        assertEquals(AvailabilityStatus.BUSY, state.slots[availabilityKey(date, 10)])
    }

    @Test
    fun `loadAvailability with empty result clears slots`() = runTest {
        val date = LocalDate.of(2026, 5, 10)
        coEvery { repository.getAvailabilitySlots(any(), any(), any()) } returns emptyList()

        viewModel.loadAvailability("trainee1", date, date)

        assertTrue(viewModel.uiState.value.slots.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadAvailability sets error on exception`() = runTest {
        val date = LocalDate.of(2026, 5, 10)
        coEvery { repository.getAvailabilitySlots(any(), any(), any()) } throws RuntimeException("Network error")

        viewModel.loadAvailability("trainee1", date, date)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed to load availability", state.error)
        assertTrue(state.slots.isEmpty())
    }

    @Test
    fun `loadAvailability clears previous error on success`() = runTest {
        val date = LocalDate.of(2026, 5, 10)

        coEvery { repository.getAvailabilitySlots(any(), any(), any()) } throws RuntimeException("error")
        viewModel.loadAvailability("trainee1", date, date)

        coEvery { repository.getAvailabilitySlots(any(), any(), any()) } returns emptyList()
        viewModel.loadAvailability("trainee1", date, date)

        assertNull(viewModel.uiState.value.error)
    }

    // ── checkAvailability ────────────────────────────────────────────────────

    @Test
    fun `checkAvailability returns true for IDEAL slot`() = runTest {
        val date = LocalDate.of(2026, 5, 10)
        coEvery { repository.getAvailabilitySlots(any(), any(), any()) } returns
            listOf(AvailabilitySlot("id1", "trainee1", date, 9, AvailabilityStatus.IDEAL))
        viewModel.loadAvailability("trainee1", date, date)

        assertTrue(viewModel.checkAvailability(date, 9))
    }

    @Test
    fun `checkAvailability returns true for MAYBE slot`() = runTest {
        val date = LocalDate.of(2026, 5, 10)
        coEvery { repository.getAvailabilitySlots(any(), any(), any()) } returns
            listOf(AvailabilitySlot("id1", "trainee1", date, 9, AvailabilityStatus.MAYBE))
        viewModel.loadAvailability("trainee1", date, date)

        assertTrue(viewModel.checkAvailability(date, 9))
    }

    @Test
    fun `checkAvailability returns false for BUSY slot`() = runTest {
        val date = LocalDate.of(2026, 5, 10)
        coEvery { repository.getAvailabilitySlots(any(), any(), any()) } returns
            listOf(AvailabilitySlot("id1", "trainee1", date, 9, AvailabilityStatus.BUSY))
        viewModel.loadAvailability("trainee1", date, date)

        assertFalse(viewModel.checkAvailability(date, 9))
    }

    @Test
    fun `checkAvailability returns true for unknown slot defaulting to IDEAL`() {
        val date = LocalDate.of(2026, 5, 10)
        assertTrue(viewModel.checkAvailability(date, 12))
    }

    // ── cycleAvailability ────────────────────────────────────────────────────

    @Test
    fun `cycleAvailability transitions IDEAL to MAYBE immediately`() = runTest {
        coEvery { repository.saveAvailabilitySlot(any()) } just Runs
        val date = LocalDate.of(2026, 5, 10)

        viewModel.cycleAvailability("trainee1", date, 9)

        assertEquals(AvailabilityStatus.MAYBE, viewModel.uiState.value.slots[availabilityKey(date, 9)])
    }

    @Test
    fun `cycleAvailability transitions MAYBE to BUSY`() = runTest {
        coEvery { repository.saveAvailabilitySlot(any()) } just Runs
        val date = LocalDate.of(2026, 5, 10)

        viewModel.cycleAvailability("trainee1", date, 9)
        viewModel.cycleAvailability("trainee1", date, 9)

        assertEquals(AvailabilityStatus.BUSY, viewModel.uiState.value.slots[availabilityKey(date, 9)])
    }

    @Test
    fun `cycleAvailability transitions BUSY back to IDEAL`() = runTest {
        coEvery { repository.saveAvailabilitySlot(any()) } just Runs
        val date = LocalDate.of(2026, 5, 10)

        viewModel.cycleAvailability("trainee1", date, 9)
        viewModel.cycleAvailability("trainee1", date, 9)
        viewModel.cycleAvailability("trainee1", date, 9)

        assertEquals(AvailabilityStatus.IDEAL, viewModel.uiState.value.slots[availabilityKey(date, 9)])
    }

    @Test
    fun `cycleAvailability does not affect other slots`() = runTest {
        coEvery { repository.saveAvailabilitySlot(any()) } just Runs
        val date = LocalDate.of(2026, 5, 10)

        viewModel.cycleAvailability("trainee1", date, 9)

        assertNull(viewModel.uiState.value.slots[availabilityKey(date, 10)])
    }

    @Test
    fun `cycleAvailability persists MAYBE slot to repository`() = runTest {
        coEvery { repository.saveAvailabilitySlot(any()) } just Runs
        val date = LocalDate.of(2026, 5, 10)

        viewModel.cycleAvailability("trainee1", date, 9)

        coVerify {
            repository.saveAvailabilitySlot(match {
                it.traineeId == "trainee1" && it.date == date && it.hour == 9 && it.status == AvailabilityStatus.MAYBE
            })
        }
    }

    @Test
    fun `cycleAvailability saves slot id as traineeId_date_hour`() = runTest {
        coEvery { repository.saveAvailabilitySlot(any()) } just Runs
        val date = LocalDate.of(2026, 5, 10)

        viewModel.cycleAvailability("trainee1", date, 9)

        coVerify {
            repository.saveAvailabilitySlot(match { it.id == "trainee1_${date}_9" })
        }
    }

    @Test
    fun `cycleAvailability multiple independent slots are tracked separately`() = runTest {
        coEvery { repository.saveAvailabilitySlot(any()) } just Runs
        val date = LocalDate.of(2026, 5, 10)

        viewModel.cycleAvailability("trainee1", date, 9)   // IDEAL -> MAYBE
        viewModel.cycleAvailability("trainee1", date, 10)  // IDEAL -> MAYBE

        assertEquals(AvailabilityStatus.MAYBE, viewModel.uiState.value.slots[availabilityKey(date, 9)])
        assertEquals(AvailabilityStatus.MAYBE, viewModel.uiState.value.slots[availabilityKey(date, 10)])
    }
}
