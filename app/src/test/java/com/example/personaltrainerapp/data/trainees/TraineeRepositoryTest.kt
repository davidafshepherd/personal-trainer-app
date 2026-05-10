package com.example.personaltrainerapp.data.trainees

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TraineeRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var repository: TraineeRepository

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        mockDb = mockk()
        every { FirebaseFirestore.getInstance() } returns mockDb
        repository = TraineeRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getAllTrainees ────────────────────────────────────────────────────────

    @Test
    fun `getAllTrainees returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getAllTrainees()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllTrainees returns empty list when query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("query error")

        val result = repository.getAllTrainees()

        assertTrue(result.isEmpty())
    }

    // ── getTraineeById ────────────────────────────────────────────────────────

    @Test
    fun `getTraineeById returns null when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getTraineeById("trainee1")

        assertNull(result)
    }

    @Test
    fun `getTraineeById returns null when document lookup throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        val result = repository.getTraineeById("nonexistent")

        assertNull(result)
    }

    // ── getRequests ──────────────────────────────────────────────────────────

    @Test
    fun `getRequests returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getRequests("trainer1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRequests returns empty list when query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("trainerRequests") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("query error")

        val result = repository.getRequests("trainer1")

        assertTrue(result.isEmpty())
    }

    // ── getTrainerAvailability ────────────────────────────────────────────────

    @Test
    fun `getTrainerAvailability returns 0 when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getTrainerAvailability("trainer1")

        assertEquals(0, result)
    }

    @Test
    fun `getTrainerAvailability returns 0 when document lookup throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        val result = repository.getTrainerAvailability("trainer1")

        assertEquals(0, result)
    }

    // ── setTrainerAvailability ────────────────────────────────────────────────

    @Test
    fun `setTrainerAvailability returns false when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.setTrainerAvailability("trainer1", 10)

        assertFalse(result)
    }

    @Test
    fun `setTrainerAvailability returns false when document update throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("update error")

        val result = repository.setTrainerAvailability("trainer1", 5)

        assertFalse(result)
    }

    // ── updateRequestStatus ───────────────────────────────────────────────────

    @Test
    fun `updateRequestStatus does not propagate exception when Firestore is unavailable`() {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.updateRequestStatus("req1", "ACCEPTED")
        // passes if no exception is thrown
    }

    @Test
    fun `updateRequestStatus does not propagate exception for REJECTED status`() {
        every { mockDb.collection(any()) } throws RuntimeException("error")

        repository.updateRequestStatus("req2", "REJECTED")
        // passes if no exception is thrown
    }
}
