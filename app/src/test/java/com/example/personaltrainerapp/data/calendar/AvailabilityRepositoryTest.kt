package com.example.personaltrainerapp.data.calendar

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AvailabilityRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var repository: AvailabilityRepository

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        mockDb = mockk()
        every { FirebaseFirestore.getInstance() } returns mockDb
        repository = AvailabilityRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getAvailabilitySlots ─────────────────────────────────────────────────

    @Test
    fun `getAvailabilitySlots returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getAvailabilitySlots("trainee1", LocalDate.now(), LocalDate.now())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailabilitySlots returns empty list when whereEqualTo query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("availabilitySlots") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("query error")

        val result = repository.getAvailabilitySlots("trainee1", LocalDate.now(), LocalDate.now())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailabilitySlots returns empty list for a multi-day range when Firestore throws`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("network error")

        val start = LocalDate.of(2026, 5, 1)
        val result = repository.getAvailabilitySlots("trainee1", start, start.plusDays(30))

        assertTrue(result.isEmpty())
    }

    // ── saveAvailabilitySlot ─────────────────────────────────────────────────

    @Test
    fun `saveAvailabilitySlot does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")
        val slot = AvailabilitySlot("id1", "trainee1", LocalDate.now(), 9, AvailabilityStatus.IDEAL)

        repository.saveAvailabilitySlot(slot)
        // passes if no exception is thrown
    }

    @Test
    fun `saveAvailabilitySlot does not propagate exception for any slot status`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("error")

        val date = LocalDate.of(2026, 5, 10)
        repository.saveAvailabilitySlot(AvailabilitySlot("id1", "trainee1", date, 9, AvailabilityStatus.MAYBE))
        repository.saveAvailabilitySlot(AvailabilitySlot("id2", "trainee1", date, 10, AvailabilityStatus.BUSY))
        repository.saveAvailabilitySlot(AvailabilitySlot("id3", "trainee1", date, 11, AvailabilityStatus.IDEAL))
        // passes if no exception is thrown
    }
}
