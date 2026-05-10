package com.example.personaltrainerapp.ui.profile

import com.example.personaltrainerapp.data.profile.ProfileRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ProfileRepository
    private lateinit var viewModel: ProfileViewModel

    private val traineeProfile = UserProfile(
        uid = "user1",
        email = "test@example.com",
        displayName = "Test User",
        bio = "My bio",
        phoneNumber = "01234 567 890",
        role = UserRole.TRAINEE,
        tags = listOf("Cardio", "Abs")
    )

    private val trainerProfile = UserProfile(
        uid = "trainer1",
        email = "trainer@example.com",
        displayName = "Test Trainer",
        role = UserRole.TRAINER,
        tags = listOf("Weight Training")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = ProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state has null profile`() {
        assertNull(viewModel.uiState.value.profile)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial state has no message`() {
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `initial state isUploadingImage is false`() {
        assertFalse(viewModel.uiState.value.isUploadingImage)
    }

    // ── seed ─────────────────────────────────────────────────────────────────

    @Test
    fun `seed sets profile when profile is null`() {
        viewModel.seed(traineeProfile)
        assertEquals(traineeProfile, viewModel.uiState.value.profile)
    }

    @Test
    fun `seed does not override existing profile when called twice`() {
        viewModel.seed(traineeProfile)
        viewModel.seed(trainerProfile)
        assertEquals(traineeProfile, viewModel.uiState.value.profile)
    }

    @Test
    fun `seed preserves all profile fields`() {
        viewModel.seed(traineeProfile)
        val profile = viewModel.uiState.value.profile!!
        assertEquals("user1", profile.uid)
        assertEquals("test@example.com", profile.email)
        assertEquals("Test User", profile.displayName)
        assertEquals("My bio", profile.bio)
        assertEquals("01234 567 890", profile.phoneNumber)
        assertEquals(UserRole.TRAINEE, profile.role)
        assertEquals(listOf("Cardio", "Abs"), profile.tags)
    }

    // ── updateDisplayName ────────────────────────────────────────────────────

    @Test
    fun `updateDisplayName applies change to local state immediately`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)

        viewModel.updateDisplayName("New Name")

        assertEquals("New Name", viewModel.uiState.value.profile?.displayName)
    }

    @Test
    fun `updateDisplayName sets success message after persist`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)

        viewModel.updateDisplayName("New Name")

        assertEquals("Display name updated.", viewModel.uiState.value.message)
    }

    @Test
    fun `updateDisplayName reverts local change on repository error`() = runTest {
        coEvery { repository.updateField(any(), any()) } throws RuntimeException("Firestore error")
        viewModel.seed(traineeProfile)

        viewModel.updateDisplayName("New Name")

        assertEquals("Test User", viewModel.uiState.value.profile?.displayName)
    }

    @Test
    fun `updateDisplayName sets error state on repository failure`() = runTest {
        coEvery { repository.updateField(any(), any()) } throws RuntimeException("Firestore error")
        viewModel.seed(traineeProfile)

        viewModel.updateDisplayName("New Name")

        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `updateDisplayName does nothing when profile is null`() = runTest {
        viewModel.updateDisplayName("Name")
        coVerify(exactly = 0) { repository.updateField(any(), any()) }
    }

    // ── updateBio ────────────────────────────────────────────────────────────

    @Test
    fun `updateBio applies change and sets message`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)

        viewModel.updateBio("Updated bio")

        assertEquals("Updated bio", viewModel.uiState.value.profile?.bio)
        assertEquals("Bio updated.", viewModel.uiState.value.message)
    }

    @Test
    fun `updateBio reverts on error`() = runTest {
        coEvery { repository.updateField(any(), any()) } throws RuntimeException("error")
        viewModel.seed(traineeProfile)

        viewModel.updateBio("Bad bio")

        assertEquals("My bio", viewModel.uiState.value.profile?.bio)
    }

    // ── updatePhoneNumber ────────────────────────────────────────────────────

    @Test
    fun `updatePhoneNumber applies change and sets message`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)

        viewModel.updatePhoneNumber("07777 000 111")

        assertEquals("07777 000 111", viewModel.uiState.value.profile?.phoneNumber)
        assertEquals("Phone number updated.", viewModel.uiState.value.message)
    }

    // ── updateTags ───────────────────────────────────────────────────────────

    @Test
    fun `updateTags for TRAINEE shows Goals message`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)

        viewModel.updateTags(listOf("Cardio", "Legs"))

        assertEquals("Goals updated.", viewModel.uiState.value.message)
    }

    @Test
    fun `updateTags for TRAINER shows Expertise message`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(trainerProfile)

        viewModel.updateTags(listOf("Strength", "HIIT"))

        assertEquals("Expertise updated.", viewModel.uiState.value.message)
    }

    @Test
    fun `updateTags applies tags to profile`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)
        val newTags = listOf("Cardio", "Legs", "Core")

        viewModel.updateTags(newTags)

        assertEquals(newTags, viewModel.uiState.value.profile?.tags)
    }

    // ── clearMessage ─────────────────────────────────────────────────────────

    @Test
    fun `clearMessage sets message to null`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)
        viewModel.updateDisplayName("Name") // populates message

        viewModel.clearMessage()

        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `clearMessage is safe to call when no message is set`() {
        viewModel.clearMessage()
        assertNull(viewModel.uiState.value.message)
    }

    // ── persistField error clearing ──────────────────────────────────────────

    @Test
    fun `second successful update clears error from previous failure`() = runTest {
        coEvery { repository.updateField("displayName", "Bad") } throws RuntimeException("error")
        coEvery { repository.updateField("displayName", "Good") } just Runs
        viewModel.seed(traineeProfile)

        viewModel.updateDisplayName("Bad")
        viewModel.updateDisplayName("Good")

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `successful update clears error field in state`() = runTest {
        coEvery { repository.updateField(any(), any()) } just Runs
        viewModel.seed(traineeProfile)

        viewModel.updateDisplayName("Name")

        assertNull(viewModel.uiState.value.error)
    }
}
