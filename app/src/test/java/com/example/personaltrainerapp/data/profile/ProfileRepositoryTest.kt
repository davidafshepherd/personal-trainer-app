package com.example.personaltrainerapp.data.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ProfileRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockStorage: FirebaseStorage
    private lateinit var mockUser: FirebaseUser
    private lateinit var repository: ProfileRepository

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseStorage::class)

        mockDb = mockk()
        mockAuth = mockk()
        mockStorage = mockk()
        mockUser = mockk()

        every { FirebaseFirestore.getInstance() } returns mockDb
        every { FirebaseAuth.getInstance() } returns mockAuth
        every { FirebaseStorage.getInstance() } returns mockStorage
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test-uid"
        every { mockUser.email } returns "test@example.com"

        repository = ProfileRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── updateField ───────────────────────────────────────────────────────────

    @Test
    fun `updateField propagates exception when Firestore collection fails`() = runTest {
        every { mockDb.collection(any()) } throws RuntimeException("Firestore error")

        try {
            repository.updateField("displayName", "New Name")
            fail("Expected RuntimeException to be propagated")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("Firestore error") == true)
        }
    }

    @Test
    fun `updateField propagates exception when document reference fails`() = runTest {
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } throws RuntimeException("document error")

        try {
            repository.updateField("bio", "My bio")
            fail("Expected RuntimeException to be propagated")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("document error") == true)
        }
    }

    // ── uid resolution ────────────────────────────────────────────────────────

    @Test
    fun `updateField throws IllegalArgumentException when user is not signed in`() = runTest {
        every { mockAuth.currentUser } returns null
        val mockCollection = mockk<CollectionReference>()
        every { mockDb.collection(any()) } returns mockCollection

        try {
            repository.updateField("displayName", "name")
            fail("Expected IllegalArgumentException from requireNotNull")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("No signed-in user") == true)
        }
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Test
    fun `changePassword throws IllegalStateException when user is not signed in`() = runTest {
        every { mockAuth.currentUser } returns null

        try {
            repository.changePassword("oldPass", "newPass")
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No signed-in user") == true)
        }
    }

    @Test
    fun `changePassword throws IllegalStateException when user has no email`() = runTest {
        every { mockUser.email } returns null

        try {
            repository.changePassword("oldPass", "newPass")
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("no email") == true)
        }
    }

    // ── uploadProfileImage ────────────────────────────────────────────────────

    @Test
    fun `uploadProfileImage propagates exception when Storage is unavailable`() = runTest {
        every { mockStorage.reference } throws RuntimeException("Storage unavailable")

        try {
            repository.uploadProfileImage(mockk())
            fail("Expected RuntimeException to be propagated")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("Storage unavailable") == true)
        }
    }
}
