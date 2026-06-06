package com.example.personaltrainerapp.data.trainer

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrainerRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var repository: TrainerRepository

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        mockDb = mockk()
        every { FirebaseFirestore.getInstance() } returns mockDb
        repository = TrainerRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getAvailableTrainers ──────────────────────────────────────────────────

    @Test
    fun `getAvailableTrainers returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getAvailableTrainers()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailableTrainers returns empty list when underlying query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("query error")

        val result = repository.getAvailableTrainers()

        assertTrue(result.isEmpty())
    }

    // ── getAllTrainers ────────────────────────────────────────────────────────

    @Test
    fun `getAllTrainers returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getAllTrainers()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllTrainers returns empty list when query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("query error")

        val result = repository.getAllTrainers()

        assertTrue(result.isEmpty())
    }

    // ── getTrainerById ────────────────────────────────────────────────────────

    @Test
    fun `getTrainerById returns null when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getTrainerById("trainer1")

        assertNull(result)
    }

    @Test
    fun `getTrainerById returns null when document lookup throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        val result = repository.getTrainerById("trainer1")

        assertNull(result)
    }

    // ── getAssignedTrainer ────────────────────────────────────────────────────

    @Test
    fun `getAssignedTrainer returns null when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getAssignedTrainer("trainee1")

        assertNull(result)
    }

    @Test
    fun `getAssignedTrainer returns null when document lookup throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        val result = repository.getAssignedTrainer("trainee1")

        assertNull(result)
    }

    // ── requestTrainer ────────────────────────────────────────────────────────

    @Test
    fun `requestTrainer does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.requestTrainer("trainee1", "trainer1")
        // passes if no exception is thrown
    }

    @Test
    fun `requestTrainer does not propagate exception when document write fails`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("trainerRequests") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("write error")

        repository.requestTrainer("trainee1", "trainer1")
        // passes if no exception is thrown
    }

    // ── unenrolFromTrainer ────────────────────────────────────────────────────

    @Test
    fun `unenrolFromTrainer does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.unenrolFromTrainer("trainee1")
        // passes if no exception is thrown
    }

    @Test
    fun `unenrolFromTrainer does not propagate exception when document write fails`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("write error")

        repository.unenrolFromTrainer("trainee1")
        // passes if no exception is thrown
    }

    // ── hasPendingTrainerRequest ──────────────────────────────────────────────

    @Test
    fun `hasPendingTrainerRequest returns false when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.hasPendingTrainerRequest("trainee1", "trainer1")

        assertFalse(result)
    }

    @Test
    fun `hasPendingTrainerRequest returns false when document lookup throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("trainerRequests") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        val result = repository.hasPendingTrainerRequest("trainee1", "trainer1")

        assertFalse(result)
    }
}
