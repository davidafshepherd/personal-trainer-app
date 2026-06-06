package com.example.personaltrainerapp.data.notifications

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var repository: NotificationRepository

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseMessaging::class)
        mockDb = mockk()
        every { FirebaseFirestore.getInstance() } returns mockDb
        every { FirebaseMessaging.getInstance() } returns mockk(relaxed = true)
        repository = NotificationRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getPreferences ────────────────────────────────────────────────────────

    @Test
    fun `getPreferences returns default when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        val result = repository.getPreferences("user1")

        assertTrue(result.emailWorkoutScheduled)
        assertTrue(result.pushWorkoutScheduled)
    }

    @Test
    fun `getPreferences returns default when document lookup throws`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        val result = repository.getPreferences("user1")

        assertEquals(NotificationPreferences(), result)
    }

    // ── savePreferences ───────────────────────────────────────────────────────

    @Test
    fun `savePreferences does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.savePreferences("user1", NotificationPreferences())
        // passes if no exception is thrown
    }

    @Test
    fun `savePreferences does not propagate exception when document write fails`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        val mockDoc = mockk<DocumentReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDoc
        every { mockDoc.set(any(), any()) } throws RuntimeException("write error")

        repository.savePreferences("user1", NotificationPreferences())
        // passes if no exception is thrown
    }

    // ── saveFcmToken ──────────────────────────────────────────────────────────

    @Test
    fun `saveFcmToken does not propagate exception when Firebase Messaging is unavailable`() = runTest {
        every { FirebaseMessaging.getInstance() } throws RuntimeException("Messaging unavailable")

        repository.saveFcmToken("user1")
        // passes if no exception is thrown
    }

    // ── createWorkoutScheduledNotifications ───────────────────────────────────

    @Test
    fun `createWorkoutScheduledNotifications does not propagate exception when Firestore is unavailable`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore unavailable")

        repository.createWorkoutScheduledNotifications("trainee1", "Leg Day")
        // passes if no exception is thrown
    }

    @Test
    fun `createWorkoutScheduledNotifications does not propagate exception when user lookup fails`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("lookup error")

        repository.createWorkoutScheduledNotifications("trainee1", "Chest Day")
        // passes if no exception is thrown
    }
}
