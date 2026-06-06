package com.example.personaltrainerapp.data.workouts

import com.example.personaltrainerapp.data.calendar.AvailabilitySlot
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RealWorkoutsRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var repository: RealWorkoutsRepository

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseStorage::class)
        mockDb = mockk()
        every { FirebaseFirestore.getInstance() } returns mockDb
        every { FirebaseStorage.getInstance() } returns mockk(relaxed = true)
        repository = RealWorkoutsRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getAllWorkouts ────────────────────────────────────────────────────────

    @Test
    fun `getAllWorkouts returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getAllWorkouts()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllWorkouts returns empty list when collection query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("workouts") } returns mockCollection
        every { mockCollection.get() } throws RuntimeException("get error")

        val result = repository.getAllWorkouts()

        assertTrue(result.isEmpty())
    }

    // ── getTrainersWorkouts ───────────────────────────────────────────────────

    @Test
    fun `getTrainersWorkouts returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getTrainersWorkouts("trainer1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTrainersWorkouts returns empty list when query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("workouts") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("query error")

        val result = repository.getTrainersWorkouts("trainer1")

        assertTrue(result.isEmpty())
    }

    // ── getUpcomingWorkouts ───────────────────────────────────────────────────

    @Test
    fun `getUpcomingWorkouts returns empty list when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getUpcomingWorkouts("trainee1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getUpcomingWorkouts returns empty list when query throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("workouts") } returns mockCollection
        every { mockCollection.whereEqualTo(any<String>(), any()) } throws RuntimeException("filter error")

        val result = repository.getUpcomingWorkouts("trainee1")

        assertTrue(result.isEmpty())
    }

    // ── getWorkoutById ────────────────────────────────────────────────────────

    @Test
    fun `getWorkoutById returns null when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getWorkoutById("w1")

        assertNull(result)
    }

    @Test
    fun `getWorkoutById returns null when document lookup throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("workouts") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        val result = repository.getWorkoutById("nonexistent")

        assertNull(result)
    }

    // ── decoupleWorkout ───────────────────────────────────────────────────────

    @Test
    fun `decoupleWorkout does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.decoupleWorkout("w1")
        // passes if no exception is thrown
    }

    @Test
    fun `decoupleWorkout does not propagate exception when document update fails`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("workouts") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("update error")

        repository.decoupleWorkout("w1")
        // passes if no exception is thrown
    }

    // ── scheduleWorkout ───────────────────────────────────────────────────────

    @Test
    fun `scheduleWorkout does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")
        val workout = Workout(id = "w1", estimatedMinutes = 60)
        val slot = AvailabilitySlot("s1", "trainee1", LocalDate.of(2026, 5, 10), 9)

        repository.scheduleWorkout(workout, "trainee1", slot)
        // passes if no exception is thrown
    }

    // ── setWorkoutById ────────────────────────────────────────────────────────

    @Test
    fun `setWorkoutById does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.setWorkoutById("w1", Workout(id = "w1"))
        // passes if no exception is thrown
    }

    @Test
    fun `setWorkoutById does not propagate exception when document set fails`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("workouts") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("set error")

        repository.setWorkoutById("w1", Workout(id = "w1"))
        // passes if no exception is thrown
    }

    // ── markExerciseComplete ──────────────────────────────────────────────────

    @Test
    fun `markExerciseComplete is a no-op when getWorkoutById returns null`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.markExerciseComplete("w1", "e1", true)
        // getWorkoutById returns null due to Firestore error; markExerciseComplete returns early
    }

    // ── saveNewWorkout ────────────────────────────────────────────────────────

    @Test
    fun `saveNewWorkout completes without exception when collection is available`() = runTest {
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        every { mockDb.collection("workouts") } returns mockCollection

        repository.saveNewWorkout(Workout(name = "Leg Day"))
        // fire-and-forget write; passes if no exception is thrown
    }
}
