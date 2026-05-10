package com.example.personaltrainerapp.data.exercises

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

class ExerciseRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var repository: ExerciseRepository

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        mockDb = mockk()
        every { FirebaseFirestore.getInstance() } returns mockDb
        repository = ExerciseRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getAllExercises ───────────────────────────────────────────────────────

    @Test
    fun `getAllExercises returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getAllExercises("trainer1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllExercises returns empty list when whereEqualTo query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("exercises") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("query error")

        val result = repository.getAllExercises("trainer1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllExercises returns empty list for any trainerId when Firestore throws`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("network error")

        val result1 = repository.getAllExercises("trainer1")
        val result2 = repository.getAllExercises("trainer2")

        assertTrue(result1.isEmpty())
        assertTrue(result2.isEmpty())
    }

    // ── saveNewExercise ───────────────────────────────────────────────────────

    @Test
    fun `saveNewExercise propagates exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        var caught = false
        try {
            val exercise = com.example.personaltrainerapp.data.workouts.Exercise(
                id = "e1", name = "Pushup", trainerId = "trainer1"
            )
            repository.saveNewExercise(exercise)
        } catch (e: RuntimeException) {
            caught = true
        }
        assertTrue(caught)
    }
}
